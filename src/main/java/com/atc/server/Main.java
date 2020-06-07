package com.atc.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class Main {

    public static void main(String[] args) throws IOException {

        GameState gameState = new GameState();

        ServerSocket ss = new ServerSocket(2137);

        while(true){
            Socket s;
            s = ss.accept();
            //System.out.println("New client connected: " + s);
            ClientConnection connection = new ClientConnection(s, gameState);
            Thread connectionThread = new Thread(connection);
            connectionThread.start();
            gameState.addConnection(s.toString(), connection);
        }
    }
}
