package com.atc.server;

import com.atc.client.model.GameSettings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
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

    private boolean passedData = false;
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
        @Override
        public void run() {
            Message message = null;
            while(!passedData){
                try {
                    message = (Message) inputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if(message.getMsgType() == Message.CLIENT_HELLO){
                    gs = message.getGameSettings();
                    clientUUID = gs.getClientUUID();
                    clientName = gs.getClientName();

                    passedData = true;
                }
            }
            gameState.generateNewAirplanes(gs.getPlaneNum(), clientUUID);
            while(passedData){
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
