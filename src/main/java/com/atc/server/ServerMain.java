package com.atc.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain implements Runnable{
    private static ServerMain singleton;
    static Thread t;
    public static ServerMain getInstance(){
        if(singleton==null){
            singleton = new ServerMain();
            t = new Thread(singleton);
            t.start();
        }
        return singleton;
    }

    static boolean running = false;


    @Override
    public void run() {

        GameState gameState = new GameState();
        ServerSocket ss;
        try {
            ss = new ServerSocket(2137);
            running=true;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(running) {
            try {
                Socket s;

                s = ss.accept();
                System.out.println("New client connected: " + s);

                ClientConnection connection = new ClientConnection(s, gameState);
                Thread connectionThread = new Thread(connection);

                connectionThread.start();

                gameState.addConnection(s.toString(), connection);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
