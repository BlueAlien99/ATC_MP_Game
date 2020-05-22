package com.atc.client.model;

import com.atc.server.Message;
import com.atc.server.model.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class HistoryStream implements Runnable{
    protected ObjectInputStream in;
    public ObjectOutputStream out;
    protected Socket socket;
    String ipAddress = "localhost";
    private int port = 2137;
    private boolean passedData = false;
    private List<Event> Events;
    private List<Integer> availableGames;
    private HashMap<UUID, String> Callsigns;
    private HashMap<Integer, String> Logins;
    private int searchedGameId;
    private Semaphore dataSemaphore = new Semaphore(0,true);
    private Semaphore initializeSemaphore = new Semaphore(0, true);

    public HistoryStream(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void sayGoodbye(){
        Message message = new Message('x');
        try {
            out.writeObject(message);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void sayHello(ObjectOutputStream out){
        Message message = new Message();
        try {
            out.writeObject(message);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void connectToSocket(String ipAddress, int port){
        try {
            socket = new Socket(ipAddress, port);
            System.out.println("Connected!");
            in = new ObjectInputStream(socket.getInputStream());
            out  = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void sendRequestForData(){
        try {
            initializeSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message message = new Message(searchedGameId);
        try {
            out.writeObject(message);
            System.out.println("Requested data!");
        }catch(IOException ex){
            ex.printStackTrace();
        }
        initializeSemaphore.release();
        try {
            dataSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        connectToSocket(ipAddress,port);
        sayHello(out);
        initializeSemaphore.release();
        System.out.println("Said hello to server");
        Message msg;
            try {
                while (!passedData) {
                    msg = (Message) in.readObject();
                    if(msg.getMsgType() == Message.GAME_HISTORY && searchedGameId >= 0) {
                        System.out.println("Received Events list from server!");
                        Events = msg.getEventsList();
                        Callsigns = msg.getCallsigns();
                        Logins = msg.getLogins();
                        dataSemaphore.release();
                    }else if(msg.getMsgType()==Message.GAME_HISTORY && searchedGameId < 0){
                        System.out.println("Received available replays from server!");
                        availableGames = msg.getAvailableGameId();
                        dataSemaphore.release();
                    } else if(msg.getMsgType() == Message.GAME_HISTORY_END){
                        System.out.println("End of exchanging data with server");
                        passedData = true;
                    }
                }
                } catch(IOException | ClassNotFoundException ex){
                    ex.printStackTrace();
                }
    }

    public List<Event> getEvents() {
        return Events;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public void setSearchedGameId(int searchedGameId) {
        this.searchedGameId = searchedGameId;
    }

    public HashMap<UUID, String> getCallsigns(){ return Callsigns;}

    public HashMap<Integer, String> getLogins() {return Logins;}

    public List<Integer> getAvailableGames() { return availableGames; }
}