package com.atc.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection implements Runnable{

    private GameState gameState;

    private final Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ClientConnection(Socket socket, GameState gameState) {
        this.gameState = gameState;
        this.socket = socket;
        try {
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        gameState.generateNewAirplanes(3, socket);
        try {
            Thread.sleep(6000);
            gameState.removeConnection(socket.toString());
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
