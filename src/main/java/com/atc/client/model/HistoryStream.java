package com.atc.client.model;

import com.atc.server.Message;
import com.atc.server.model.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HistoryStream{
    protected ObjectInputStream in;
    public ObjectOutputStream out;
    protected Socket socket;
    String ipAddress = "localhost";
    private int port = 2137;
    private List<Event> Events;
    private HashMap<UUID, String> Callsigns;
    private HashMap<Integer, String> Logins;
    private int searchedGameId;

    public HistoryStream(String ipAddress) {
        this.ipAddress = ipAddress;
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

    private void sendRequestForData(int searchedGameId){
        Message message = new Message(searchedGameId);
        try {
            out.writeObject(message);
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    public void run(){
        connectToSocket(ipAddress,port);
        sayHello(out);
        System.out.println("Said hello to server");
        sendRequestForData(searchedGameId);
        System.out.println("Requested data!");
        Message msg;
            try {
                while (Events == null) {
                    msg = (Message) in.readObject();
                    if(msg.getMsgType()==Message.GAME_HISTORY) {
                        System.out.println("Received data from server!");
                        Events = msg.getEventsList();
                        Callsigns = msg.getCallsigns();
                        Logins = msg.getLogins();
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

    public HashMap<UUID, String> getCallsigns() {
        return Callsigns;
    }

    public HashMap<Integer, String> getLogins() {
        return Logins;
    }
}
