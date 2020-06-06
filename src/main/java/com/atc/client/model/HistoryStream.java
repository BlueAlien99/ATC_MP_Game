package com.atc.client.model;

import com.atc.server.Message;
import com.atc.server.model.Event;
import com.atc.server.model.Login;
import com.atc.server.model.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static com.atc.server.Message.msgTypes.*;

public class HistoryStream extends StreamController{
    protected ObjectInputStream in;
    public ObjectOutputStream out;
    protected Socket socket;
    String ipAddress;
    private int port = 2137;
    private boolean passedData = false;
    private boolean terminated = false;
    private List<Checkpoint> checkpoints;
    private List<Event> Events;
    private List<Integer> availableGames;
    private List<Player> playersList;
    private HashMap<UUID, String> Callsigns;
    private HashMap<Integer, String> Logins;
    private List<Login> bestScoresLoginsList;
    private int searchedGameId;
    private Semaphore dataSemaphore = new Semaphore(0,true);
    private Semaphore initializeSemaphore = new Semaphore(0, true);

    public HistoryStream(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void sayGoodbye(){
        Message message = new Message(GAME_HISTORY_END);
        try {
            out.writeObject(message);
            initializeSemaphore.release();
            dataSemaphore.release();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void sayHello(ObjectOutputStream out) throws IOException{
        Message message = new Message(CLIENT_HELLO);
        out.writeObject(message);
    }

    private void connectToSocket(String ipAddress, int port) throws IOException{
            socket = new Socket(ipAddress, port);
            System.out.println("Connected!");
            in = new ObjectInputStream(socket.getInputStream());
            out  = new ObjectOutputStream(socket.getOutputStream());
    }

    public void askForPlayers() throws InterruptedException, IOException {
        initializeSemaphore.acquire();
        Message message = new Message(FETCH_PLAYERS);
        out.writeObject(message);
        System.out.println("Requested data!");
        initializeSemaphore.release();
        dataSemaphore.acquire();
    }

    public void sendRequestForData() throws InterruptedException, IOException{
        initializeSemaphore.acquire();
        Message message = new Message(searchedGameId);
        out.writeObject(message);
        System.out.println("Requested data!");
        initializeSemaphore.release();
        dataSemaphore.acquire();
    }
    @Override
    public void run(){
        try{
        connectToSocket(ipAddress,port);
        sayHello(out);
        initializeSemaphore.release();
        System.out.println("Said hello to server");
        Message msg;
                while (!passedData && !terminated) {
                    msg = (Message) in.readObject();
                    if (msg.getMsgType() == GAME_HISTORY && searchedGameId >= 0) {
                        System.out.println("Received Events list from server!");
                        Events = msg.getEventsList();
                        Callsigns = msg.getCallsigns();
                        Logins = msg.getLogins();
                        checkpoints = msg.getDbCheckpoints();
                        dataSemaphore.release();
                    } else if (msg.getMsgType() == GAME_HISTORY && searchedGameId < 0) {
                        System.out.println("Received available replays from server!");
                        availableGames = msg.getAvailableGameId();
                        dataSemaphore.release();
                    } else if (msg.getMsgType() == PLAYERS_LIST){
                        playersList = msg.getPlayersList();
                        bestScoresLoginsList = msg.getBestScoresLoginList();
                        dataSemaphore.release();
                    }else if(msg.getMsgType() == GAME_HISTORY_END){
                        System.out.println("End of exchanging data with server");
                        passedData = true;
                    }
                }
                } catch(IOException | ClassNotFoundException ex){
                    initializeSemaphore.release();
                    dataSemaphore.release();
                }
    }


    @Override
    public void terminate() {
        terminated = true;
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

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void setSearchedGameId(int searchedGameId) {
        this.searchedGameId = searchedGameId;
    }

    public HashMap<UUID, String> getCallsigns(){ return Callsigns;}

    public HashMap<Integer, String> getLogins() {return Logins;}

    public List<Integer> getAvailableGames() { return availableGames; }

    public List<Player> getPlayersList() {
        return playersList;
    }

    public List<Login> getBestScoresLoginsList() {
        return bestScoresLoginsList;
    }
}
