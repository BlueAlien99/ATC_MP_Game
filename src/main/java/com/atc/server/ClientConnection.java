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

/**
 * Class with a state machine, that allows easier handling of connection client-server.
 */

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


    private UUID clientUUID = UUID.randomUUID();
    private String clientName;
    private GameSettings gs;

    private Thread input;
    private Thread output;

    /**
     * Constructor of client Connection.
     * @param socket - client's socket
     * @param gameState - gameState
     */

    public ClientConnection(Socket socket, GameState gameState) {
        this.gameState = gameState;
        this.outputBufferLock = gameState.getOutputBufferLock();
        this.socket = socket;
    }

    /**
     * Ends connection with server.
     * @throws IOException exception
     */

    public void disconnect() throws IOException {
        outputStream.writeObject(new Message(DISCONNECT));
        output.interrupt();
        input.interrupt();
        socket.close();
    }

    /**
     * Initialize input and output stream
     */
    @Override
    public void run() {
        try {
            ////System.out.println("Streams being created! - " + socket.toString());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            ////System.out.println("Streams created! - " + socket.toString());
        } catch(IOException e){
            e.printStackTrace();
        }
        input = new Thread(new Input());
        input.start();

        output = new Thread(new Output());
        output.start();
    }

    /**
     * Input stream class - through this stream messages form client to server and vice versa are transported to their receivers.
     */
    private class Input implements Runnable{
        /**
         * Stores data passed in message with game settings on server, (MULTIPLAYER GAME VERSION)
         * @param message message
         */

        private void passSettings(Message message){
            gs = message.getGameSettings();
            clientUUID = gs.getClientUUID();
            clientName = gs.getClientName();
            if(gameState.searchPlayerLogin(clientUUID)==null){
                gameState.addPlayerLogin(clientUUID, clientName);
                ////System.out.println("Client passed settings!");
                gameState.generateNewAirplanes(gs.getPlaneNum(), clientUUID);
                gameState.sendMessageToAll(clientName + " connected!");
            }
            else{
                gameState.sendMessageToAll(clientName + " is back!");
            }
        }

        /**
         * Stores data passed in message with game settings and game scenario on server, (SINGLEPLAYER GAME VERSION)
         * @param message message
         */
        private void passInitial(Message message){
            gs = message.getGameSettings();
            clientUUID = gs.getClientUUID();
            clientName = gs.getClientName();
            message.getCheckpoints().forEach((uuid, checkpoint) -> gameState.addCheckpoint(checkpoint));
            message.getAirplanes().forEach((uuid, airplane)-> gameState.addAirplane(airplane));
            if(gameState.searchPlayerLogin(clientUUID)==null){
                gameState.addPlayerLogin(clientUUID, clientName);
                ////System.out.println("Client passed singleplayer data!");
                gameState.sendMessageToAll(clientName + " connected!");
            }
            else{
                gameState.sendMessageToAll(clientName + " is back!");
            }
        }

        /**
         * Gets data about players and their logins from database and sends them to client
         * @return created message
         */

        private Message sendPlayers(){
            List<Player> plavers = gameState.getLog().selectAllPlayers();
            List<Login> logins = gameState.getLog().selectAllLogins();
            //System.out.println("Got data about players from database!");
            Message msg = new Message(PLAYERS_LIST);
            msg.setPlayersList(plavers);
            msg.setBestScoresLoginList(logins);
            return msg;
        }

        /**
         * Sends list of available game replays to client.
         * @return created message
         */
        private Message sendAvailableReplays(){
            List<Integer> availableGames = gameState.getLog().selectAvailableGameId();
            //System.out.println("Got data from database!");
            return new Message(availableGames);
        }

        /**
         * Sends data about the requested gameplay to client.
         * @param searchedGameId - requested gameID
         * @return created message
         */
        private Message sendDataAboutGame(int searchedGameId){
            List<Checkpoint> checkpoints = gameState.getLog().selectCheckpoints(searchedGameId);
            List<Event> Events = gameState.getLog().selectGameIdEvents(searchedGameId);
            HashMap<Integer, String> Logins = gameState.getLog().selectPlayerLogin(searchedGameId);
            HashMap<UUID, String> Callsigns = gameState.getLog().selectAirplaneCallsigns(searchedGameId);
            //System.out.println("Got data from database!");
            return new Message(searchedGameId, Events, Callsigns, Logins, checkpoints);
        }

        /**
         * State machine itself - depending on what was the last message and state it manages events occuring in current game,
         * data exchange with database etc.
         * <br>
         *     CONNECTION_IDLE - server waits for client to make a next move
         * <br>
         *     CONNECTION_HISTORY - server handles data transactions with database
         * <br>
         *     CONNECTION_GAME - server manages events in game
         */
        @Override
        public void run() {
            Message message = null;
            //this "thing" below is a state machine. Looks how it looks but allows for easier handling than previous ideas
            while(!Thread.currentThread().isInterrupted()){
                try{
                    if(socket.isClosed()) {
                        gameState.removeConnection(socket.toString());
                        output.interrupt();
                        break;
                    }
                    message = (Message) inputStream.readObject();
                    System.out.println("Connection mode: "+connectionMode);
                }
                catch (EOFException ignored) {}
                catch(SocketException sex){
                    try{
                        //System.out.println("Socket close input.run() 1");
                        socket.close();}
                    catch(Exception es) {
                        //System.out.println("Can't close connection");}
                    }
                    //System.out.println("Closing socket to "+ clientUUID.toString());
                    gameState.removeConnection(socket.toString());
                    break;
                }
                catch(Exception e){
                    gameState.removeConnection(socket.toString());
                    e.printStackTrace();
                    break;
                }
                if(message == null){
                    continue;
                }
                lastMsgType = message.getMsgType();

                if(lastMsgType==DISCONNECT){
                    output.interrupt();
                    gameState.removeConnection(socket.toString());
                    return;
                }

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
                        Message outMsg = new Message(gameState.getAirplanesList());
//                        Message outMsg = new Message(gameState.getAirplanesOutput());
                        try {
                            outputStream.writeObject(outMsg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("Sent airplanes to client");
                        continue;
                    }
                    if(lastMsgType == FETCH_CHECKPOINTS){
                        Message outMsg = new Message(CHECKPOINTS_LIST);
                        outMsg.setCheckpoints(gameState.getCheckpointsList());
                        outMsg.setCheckpointsAirplanesMapping(gameState.getCheckpointsAirplanesMapping());
                        try {
                            outputStream.writeObject(outMsg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("Sent checkpoints to client");
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
                            //System.out.println("Sent data to client!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    if(lastMsgType == GAME_HISTORY_END) {
                        try {
                            outputStream.writeObject(new Message(Message.msgTypes.GAME_HISTORY_END));
                            //System.out.println("Stopped exchanging data with client!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        searchedGameId=0;
                        connectionMode=CONNECTION_IDLE;
                        continue;
                    }
                    if(lastMsgType == CLIENT_GOODBYE){
                        connectionMode=CONNECTION_IDLE;
                        continue;
                    }
                }
            }
            try {
                if(!socket.isClosed()) {
                    //System.out.println("Socket close input.run() 2");
                    socket.close();
                    gameState.removeConnection(socket.toString());
                }
            } catch (IOException e) {
                gameState.removeConnection(socket.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     *  Output stream class - through this client and server can write messages to each other.
     *
     */

    private class Output implements Runnable{
        @Override
        public void run() {
            ConcurrentHashMap<Integer, String> chatMsg = gameState.getChatMessages();
            run: while(!Thread.currentThread().isInterrupted()){
                if(currentTick != gameState.getTickCount() && connectionMode == CONNECTION_GAME){
                    currentTick = gameState.getTickCount();
                    Message msg = new Message(gameState.getAirplanesOutput());
                    try {
                        outputStream.writeObject(msg);
                    }
                    catch(SocketException sex){
                        try{
                            //System.out.println("Socket close output.run() 1");
                            socket.close();
                        }
                        catch(Exception es) {
                            //System.out.println("Can't close connection");
                        }
                        //System.out.println("Closing socket to "+ clientUUID.toString());
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
                            return;
                        }
                    }
                }
            }
            gameState.removeConnection(socket.toString());
            try {
                //System.out.println("Socket close output.run() 2");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
