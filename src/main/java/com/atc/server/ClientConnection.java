package com.atc.server;

import com.atc.client.model.Checkpoint;
import com.atc.client.model.GameSettings;
import com.atc.server.model.Event;
import com.atc.server.model.Login;
import com.atc.server.model.Player;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.server.Message.msgTypes.*;

//TODO: Safe casting and proper connection closing!
public class ClientConnection implements Runnable{

    public final static int CONNECTION_IDLE = 0;
    public final static int CONNECTION_GAME = 1;
    public final static int CONNECTION_HISTORY = 2;

    private final GameState gameState;
    private final Object outputBufferLock;

    private final Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private int currentTick = 0;
    private int currentChatMsg = 0;

    private Message.msgTypes lastMsgType;

    private boolean clientHello = false;
    private int connectionMode = CONNECTION_IDLE;
    private int searchedGameId = 0;


    private UUID clientUUID;
    private String clientName;
    private GameSettings gs;

    private Thread input;
    private Thread output;

    public ClientConnection(Socket socket, GameState gameState) {
        this.gameState = gameState;
        this.outputBufferLock = gameState.getOutputBufferLock();
        this.socket = socket;
    }

    public String getClientName() {
        return clientName;
    }

    public GameSettings getGs(){
        return gs;
    }

    @Override
    public void run() {
        try {
            System.out.println("Streams being created! - " + socket.toString());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Streams created! - " + socket.toString());
        } catch(IOException e){
            e.printStackTrace();
        }
        input = new Thread(new Input());
        input.start();

        output = new Thread(new Output());
        output.start();
    }

    //TODO: Test syncing, eg. getTickCount()!
    //TODO: Validate and improve connection loss detection!
    private class Input implements Runnable{

        private void passSettings(Message message){
            gs = message.getGameSettings();
            clientUUID = gs.getClientUUID();
            clientName = gs.getClientName();
            if(gameState.searchPlayerLogin(clientUUID)==null){
                gameState.addPlayerLogin(clientUUID, clientName);
                System.out.println("Client passed settings!");
                gameState.generateNewAirplanes(gs.getPlaneNum(), clientUUID);
                gameState.sendMessageToAll(clientName + " connected!");
            }
            else{
                gameState.sendMessageToAll(clientName + " is back!");
            }
        }

        private void passInitial(Message message){
            gs = message.getGameSettings();
            clientUUID = gs.getClientUUID();
            clientName = gs.getClientName();
            message.getCheckpoints().forEach((uuid, checkpoint) -> {gameState.addCheckpoint(checkpoint);});
            message.getAirplanes().forEach((uuid, airplane)->{
                gameState.addAirplane(airplane);
            });
            if(gameState.searchPlayerLogin(clientUUID)==null){
                gameState.addPlayerLogin(clientUUID, clientName);
                System.out.println("Client passed singleplayer data!");
                gameState.sendMessageToAll(clientName + " connected!");
            }
            else{
                gameState.sendMessageToAll(clientName + " is back!");
            }
        }

        private Message sendPlayers(){
            List<Player> plavers = gameState.getLog().selectAllPlayers();
            List<Login> logins = gameState.getLog().selectAllLogins();
            System.out.println("Got data about players from database!");
            Message msg = new Message(PLAYERS_LIST);
            msg.setPlayersList(plavers);
            msg.setBestScoresLoginList(logins);
            return msg;
        }

        private Message sendAvailableReplays(){
            List<Integer> availableGames = gameState.getLog().selectAvailableGameId();
            System.out.println("Got data from database!");
            return new Message(availableGames);
        }

        private Message sendDataAboutGame(int searchedGameId){
            List<Checkpoint> checkpoints = gameState.getLog().selectCheckpoints(searchedGameId);
            List<Event> Events = gameState.getLog().selectGameIdEvents(searchedGameId);
            HashMap<Integer, String> Logins = gameState.getLog().selectPlayerLogin(searchedGameId);
            HashMap<UUID, String> Callsigns = gameState.getLog().selectAirplaneCallsigns(searchedGameId);
            System.out.println("Got data from database!");
            return new Message(searchedGameId, Events, Callsigns, Logins, checkpoints);
        }

        @Override
        public void run() {
            Message message = null;
            //this "thing" below is a state machine. Looks how it looks but allows for easier handling than previous ideas
            while(true){
                try{
                    if(socket.isClosed()) {
                        gameState.removeConnection(socket.toString());
                        break;
                    }
                    message = (Message) inputStream.readObject();
                }
                catch (EOFException ignored) {}
                catch(SocketException sex){
                    try{
                        System.out.println("Socket close input.run() 1");
                        socket.close();}
                    catch(Exception es) {System.out.println("Can't close connection");}
                    System.out.println("Closing socket to "+ clientUUID.toString());
                    gameState.removeConnection(socket.toString());
                    break;
                }
                catch(Exception e){
                    e.printStackTrace();
                    break;
                }
                if(message == null){
                    continue;
                }
                lastMsgType = message.getMsgType();

                if(!clientHello){
                    if (lastMsgType==CLIENT_HELLO)
                        clientHello=true;
                    continue;
                }

                if(connectionMode == CONNECTION_IDLE){
                    if (lastMsgType==CLIENT_SETTINGS) {
                        passSettings(message);
                        connectionMode = CONNECTION_GAME;
                        gameState.incCurrPlaying();
                    }
                    if (lastMsgType==SEND_INITIAL) {
                        passInitial(message);
                        connectionMode = CONNECTION_GAME;
                        gameState.incCurrPlaying();
                    }
                    if (lastMsgType==GAME_HISTORY || lastMsgType == FETCH_PLAYERS){
                        connectionMode = CONNECTION_HISTORY;
                    }
                }

                if(connectionMode == CONNECTION_GAME){
                    if(lastMsgType == GAME_PAUSE){
                        gameState.simulationPauseResume();
                        continue;
                    }
                    if(lastMsgType == GAME_RESUME){
                        gameState.simulationPauseResume();
                        continue;
                    }
                    if(lastMsgType == AIRPLANE_COMMAND){
                        gameState.updateAirplane(message.getUpdatedAirplane(), clientUUID);
                        continue;
                    }
                    if(lastMsgType == FETCH_AIRPLANES){
                        Message outMsg = new Message(gameState.getAirplanes());
                        try {
                            outputStream.writeObject(outMsg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Sent airplanes to client");
                        continue;
                    }
                    if(lastMsgType == FETCH_CHECKPOINTS){
                        Message outMsg = new Message(CHECKPOINTS_LIST);
                        outMsg.setCheckpoints(gameState.getCheckpoints());
                        outMsg.setCheckpointsAirplanesMapping(gameState.getCheckpointsAirplanesMapping());
                        try {
                            outputStream.writeObject(outMsg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Sent checkpoints to client");
                        continue;
                    }
                    if(lastMsgType == CLIENT_GOODBYE){
                        gameState.sendMessageToAll(clientName + " left :(");
                        connectionMode=CONNECTION_IDLE;
                        gameState.decCurrPlaying();
                        continue;
                    }
                }

                if(connectionMode == CONNECTION_HISTORY){
                    if(lastMsgType == GAME_HISTORY) {
                        searchedGameId = message.getGameid();
                        Message gameHistoryMessage;
                        if (searchedGameId < 0)
                            gameHistoryMessage = sendAvailableReplays();
                        else
                            gameHistoryMessage = sendDataAboutGame(searchedGameId);
                        try {
                            outputStream.writeObject(gameHistoryMessage);
                            System.out.println("Sent data to client!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }else if(lastMsgType == FETCH_PLAYERS){
                        Message playersMessage = sendPlayers();
                        try {
                            outputStream.writeObject(playersMessage);
                            System.out.println("Sent data to client!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    if(lastMsgType == GAME_HISTORY_END) {
                        try {
                            outputStream.writeObject(new Message(Message.msgTypes.GAME_HISTORY_END));
                            System.out.println("Stopped exchanging data with client!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        searchedGameId=0;
                        connectionMode=CONNECTION_IDLE;
                        continue;
                    }
                }
            }
            try {
                if(!socket.isClosed()) {
                    System.out.println("Socket close input.run() 2");
                    socket.close();
                    gameState.removeConnection(socket.toString());
                }
            } catch (IOException e) {
                gameState.removeConnection(socket.toString());
                e.printStackTrace();
            }
        }
    }

    private class Output implements Runnable{
        @Override
        public void run() {
            ConcurrentHashMap<Integer, String> chatMsg = gameState.getChatMessages();
            run: while(true){
                if(currentTick != gameState.getTickCount() && connectionMode == CONNECTION_GAME){
                    currentTick = gameState.getTickCount();
                    Message msg = new Message(gameState.getAirplanesOutput());
                    try {
                        outputStream.writeObject(msg);
                    }
                    catch(SocketException sex){
                        try{
                            System.out.println("Socket close output.run() 1");
                            socket.close();
                        }
                        catch(Exception es) {System.out.println("Can't close connection");}
                        System.out.println("Closing socket to "+ clientUUID.toString());
                        gameState.removeConnection(socket.toString());
                        break;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                if(currentChatMsg != chatMsg.size()){
                    while(currentChatMsg < chatMsg.size()){
                        Message msg = new Message(chatMsg.get(currentChatMsg));
                        try {
                            outputStream.writeObject(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break run;
                        }
                        ++currentChatMsg;
                    }
                }
                if(gameState.getCheckpointsUpdated()){
                    Message msg = new Message(CHECKPOINTS_LIST);
                    msg.setCheckpoints(gameState.getCheckpoints());
                    msg.setCheckpointsAirplanesMapping(gameState.getCheckpointsAirplanesMapping());
                    try {
                        outputStream.writeObject(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    gameState.setCheckpointsUpdated(false);

                }
                synchronized (outputBufferLock){
                    if(currentTick == gameState.getTickCount() && currentChatMsg == chatMsg.size()){
                        try {
                            outputBufferLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            gameState.removeConnection(socket.toString());
            try {
                System.out.println("Socket close output.run() 2");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
