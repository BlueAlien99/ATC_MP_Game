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

    public void interrupt(){
        try {
            gameState.disconnectAll();
            ss.close();
        } catch (IOException ignored) {}
        t.interrupt();
    }

    static boolean running = false;
    private ServerSocket ss;
    private GameState gameState = new GameState();


    @Override
    public void run() {
        try {
            ss = new ServerSocket(2137);
            running=true;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(running && !Thread.currentThread().isInterrupted()) {
            try {
                Socket s;

                s = ss.accept();
                System.out.println("New client connected: " + s);

                ClientConnection connection = new ClientConnection(s, gameState);
                Thread connectionThread = new Thread(connection);

                connectionThread.start();

                gameState.addConnection(s.toString(), connection);
            }
            catch (IOException ex) {
                if(!ss.isClosed())
                    ex.printStackTrace();
            }
        }
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
