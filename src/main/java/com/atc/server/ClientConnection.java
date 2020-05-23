package com.atc.server;

import com.atc.client.model.GameSettings;
import com.atc.server.model.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

//TODO: Safe casting and proper connection closing!
public class ClientConnection implements Runnable{

    private final GameState gameState;
    private final Object outputBufferLock;

    private final Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private int currentTick = 0;
    private int currentChatMsg = 0;

    private boolean clientHello = false;
    private boolean passedGameSettings = false;
    private UUID clientUUID;
    private String clientName;
    private GameSettings gs;

    public ClientConnection(Socket socket, GameState gameState) {
        this.gameState = gameState;
        this.outputBufferLock = gameState.getOutputBufferLock();
        this.socket = socket;
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
        Thread input = new Thread(new Input());
        input.start();
        Thread output = new Thread(new Output());
        output.start();
//        try {
//            Thread.sleep(6000);
//            gameState.removeConnection(socket.toString());
//            socket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    //TODO: Test syncing, eg. getTickCount()!
    //TODO: Validate and improve connection loss detection!
    private class Input implements Runnable{

        private void chooseGameMode(){
            Message message = null;
            try {
                message = (Message) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (message.getMsgType() == Message.CLIENT_SETTINGS){
                clientPassedSettings(message);
            } else if(message.getMsgType() == Message.GAME_HISTORY) {
                clientWantsData(message);
            }

        }

        private void clientSaidHello(Message message){
            while(!clientHello){
                try {
                    message = (Message) inputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if(message.getMsgType() == Message.CLIENT_HELLO){
                    clientHello = true;
                }
            }
        }
        private void clientPassedSettings(Message message){
            gs = message.getGameSettings();
            gameState.setGameSettings(gs);
            clientUUID = gs.getClientUUID();
            clientName = gs.getClientName();
            gameState.addPlayerLogin(clientUUID, clientName);
            passedGameSettings = true;
            System.out.println("Client passed settings!");
            gameState.generateNewAirplanes(gs.getPlaneNum(), clientUUID);
        }

        private Message sendAvailableReplays(){
;           List<Integer> availableGames = gameState.getLog().selectAvailableGameId();
            System.out.println("Got data from database!");
            return new Message(availableGames);
        }

        private Message sendDataAboutEvents(int searchedGameId){
            List<Event> Events = gameState.getLog().selectGameIdEvents(searchedGameId);
            HashMap<Integer, String> Logins = gameState.getLog().selectPlayerLogin(searchedGameId);
            HashMap<UUID, String> Callsigns = gameState.getLog().selectAirplaneCallsigns(searchedGameId);
            System.out.println("Got data from database!");
            return new Message(searchedGameId, Events, Callsigns, Logins);
        }

        private void clientWantsData(Message message){
            while(message.getMsgType() != Message.GAME_HISTORY_END){
                Message  gameHistoryMessage;
                System.out.println("Client wants data!");
                int searchedGameId = message.getGameid();
                if(searchedGameId < 0)
                    gameHistoryMessage = sendAvailableReplays();
                else
                    gameHistoryMessage = sendDataAboutEvents(searchedGameId);
                try {
                    outputStream.writeObject(gameHistoryMessage);
                    System.out.println("Sent data to client!");
                    message = (Message) inputStream.readObject();

                } catch (SocketException sex) {
                    try {
                        socket.close();
                    } catch (Exception es) {
                        System.out.println("Can't close connection");
                    }
                    System.out.println("Closing socket to " + clientUUID.toString());
                    gameState.removeConnection(socket.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //END OF WHILE WE HAVE TO SAY GOODBYE TO CLIENT
            try {
                outputStream.writeObject(new Message('c'));
                System.out.println("Stopped exchanging data with client");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            Message message = null;
            clientSaidHello(message);
            System.out.println("Client said hello!");
            chooseGameMode();
            while(passedGameSettings){
                try{
                    message = (Message) inputStream.readObject();
                }
                catch(SocketException sex){
                    try{socket.close();}
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
                    break;
                }
                if(message.getMsgType() == Message.AIRPLANE_COMMAND){
                    gameState.updateAirplane(message.getUpdatedAirplane(), clientUUID);
                }
            }
            gameState.removeConnection(socket.toString());
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Output implements Runnable{
        @Override
        public void run() {
            ConcurrentHashMap<Integer, String> chatMsg = gameState.getChatMessages();
            while(true){
                if(currentTick != gameState.getTickCount()){
                    currentTick = gameState.getTickCount();
                    Message msg = new Message(gameState.getAirplanesOutput());
                    try {
                        outputStream.writeObject(msg);
                    }
                    catch(SocketException sex){
                        try{socket.close();}
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
                            break;
                        }
                        ++currentChatMsg;
                    }
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
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
