package com.atc.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        gameState.generateNewAirplanes(3, socket);
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
            Message message;
            while(true){
                try{
                    message = (Message) inputStream.readObject();
                } catch(Exception e){
                    e.printStackTrace();
                    break;
                }
                if(message == null){
                    break;
                }
                if(message.getMsgType() == Message.AIRPLANE_COMMAND){
                    gameState.updateAirplane(message.getUpdatedAirplane(), socket);
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
                    } catch (IOException e) {
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
