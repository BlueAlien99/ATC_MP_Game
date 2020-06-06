package com.atc.client.model;

import com.atc.client.controller.GameCreatorController;
import com.atc.server.Message;
import com.atc.server.model.Event;
import com.atc.server.model.Login;
import com.atc.server.model.Player;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static com.atc.server.Message.msgTypes.*;

public class ClientStreamHandler implements Runnable {
    public static ClientStreamHandler singleton;
    public static Thread thread;

    private static String ipAddress = "localhost";

    public static ClientStreamHandler getInstance(){
        if(thread!=null && singleton!=null) {
            if(thread.isAlive())
            return singleton;
        }
        singleton = new ClientStreamHandler();
        if(thread!=null)
            thread.interrupt();
        thread = new Thread(singleton);
        thread.start();
        return singleton;
    }

    public void setChatBox(VBox chatBox) {
        this.chatBox = chatBox;
    }

    public void setGameActivity(GameActivity gameActivity) {
        this.gameActivity = gameActivity;
    }

    //Below are the fields as needed by the GameActivityController
    public Semaphore streamNotifier = new Semaphore(0);
    private VBox chatBox;
    private GameActivity gameActivity;

    //Below are the fields for use with GameHistory and their respective getters and setters
    private boolean passedData = false;
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


    public List<Event> getEvents() {
        return Events;
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



    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public enum StreamStates {STREAM_HISTORY, STREAM_GAME, STREAM_IDLE};
    private StreamStates streamState = StreamStates.STREAM_IDLE;


    public boolean connected = false;
    private boolean terminated = false;

    private Message.msgTypes lastMsgType;

    @Override
    public void run() {
        try {
            System.out.println(GameSettings.getInstance().getClientUUID().toString());
            socket = new Socket("localhost", 2137);
            System.out.println("Connected!");
            System.out.println(socket.toString());
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("Unable to connect. Are you sure about IP address?");
        }

        if(in==null || out==null || socket.isClosed()) {
            streamNotifier.release();
            return;
        }
        streamNotifier.release();
        connected=true;

        while (connected && !terminated) {
            Message msg = null;
            try {
                msg = (Message) in.readObject();
            } catch (IOException e) {
                resetConnection();
            }
            catch (ClassNotFoundException e){
                e.printStackTrace();
            }
            if (msg == null)
                continue;
            lastMsgType = msg.getMsgType();
            try {
                switch (streamState) {
                    case STREAM_IDLE:
                        idleSwitch(msg);
                        break;
                    case STREAM_HISTORY:
                        historySwitch(msg);
                        break;
                    case STREAM_GAME:
                        gameSwitch(msg);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

    }

    private void idleSwitch(Message msg){
        switch (lastMsgType){
            case DISCONNECT:
                GameSettings.getInstance().setIpAddress("localhost");
                try {
                    updateIP();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void historySwitch(Message msg) throws IOException {
        switch (lastMsgType){
            case GAME_HISTORY:
                if(searchedGameId >= 0){
                    System.out.println("Received Events list from server!");
                    Events = msg.getEventsList();
                    Callsigns = msg.getCallsigns();
                    Logins = msg.getLogins();
                    checkpoints = msg.getDbCheckpoints();
                    dataSemaphore.release();
                }
                else{
                    System.out.println("Received available replays from server!");
                    availableGames = msg.getAvailableGameId();
                    dataSemaphore.release();
                }
                break;
            case PLAYERS_LIST:
                playersList = msg.getPlayersList();
                bestScoresLoginsList = msg.getBestScoresLoginList();
                dataSemaphore.release();
                break;
            case GAME_HISTORY_END:
                System.out.println("End of exchanging data with server");
                setStreamState(StreamStates.STREAM_IDLE);
                break;
        }
    }

    private void gameSwitch(Message msg) throws IOException {
        switch (lastMsgType) {
            case AIRPLANES_LIST:
                gameActivity.updateAirplanes(msg.getAirplanes());
                Platform.runLater(
                        () -> gameActivity.wrapPrinting());
                msg.getAirplanes().forEach((k,v)->{
                    System.out.println(v.getCallsign()+" "+v.getPosX()+" "+v.getPosY());
                });
                System.out.println("Got airplanes");
                break;
            case CHECKPOINTS_LIST:
                ConcurrentHashMap<UUID, Checkpoint> tempMap = msg.getCheckpoints();
                msg.getCheckpointsAirplanesMapping().forEach(pair ->
                        tempMap.get(pair.getKey()).passAirplane(pair.getValue()));
                gameActivity.setCheckpoints(tempMap);
                Platform.runLater(
                        () -> gameActivity.wrapPrinting());
                System.out.println("Got checkpoints");
                break;
            case CHAT_MESSAGE:
                Label msgLabel = new Label(msg.getChatMsg());
                msgLabel.setFont(new Font("Comic Sans MS", 14));
                msgLabel.setTextFill(Color.GREEN);
                Platform.runLater(
                        () -> chatBox.getChildren().add(msgLabel)
                );
                break;
            case SERVER_GOODBYE:
                setStreamState(StreamStates.STREAM_IDLE);
                break;
        }
    }


    public void askForPlayers() throws InterruptedException, IOException {
        initializeSemaphore.acquire();
        Message message = new Message(FETCH_PLAYERS);
        out.writeObject(message);
        System.out.println("Requested data!");
        initializeSemaphore.release();
        dataSemaphore.acquire();
    }

    public void sendRequestForData() throws InterruptedException, IOException {
        initializeSemaphore.acquire();
        Message message = new Message(searchedGameId);
        out.writeObject(message);
        System.out.println("Requested data!");
        initializeSemaphore.release();
        dataSemaphore.acquire();
    }

    private void resetConnection(){
        if(!socket.isClosed())
            return;
        try {
            socket = new Socket(GameSettings.getInstance().getIpAddress(), 2137);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            sayHello();
        } catch (IOException e) {
            GameSettings.getInstance().setIpAddress("localhost");

        }
    }

    public void setStreamState(StreamStates newState) throws IOException {
        if(streamState==newState)
            return;
        switch (streamState){
            case STREAM_HISTORY:
                sayGoodbye();
                break;
            case STREAM_GAME:
                out.writeObject(new Message(Message.msgTypes.CLIENT_GOODBYE));
                break;
            case STREAM_IDLE:
                //do nothing really, as server does not need to be notified right now
                break;
        }
        streamState = newState;
        switch (streamState){
            case STREAM_HISTORY:
                sayHello();
                initializeSemaphore.release();
                break;
            case STREAM_GAME:
                try {
                    sayHello();
                    if(!GameCreatorController.creatorMessage.msgSet)
                        out.writeObject(new Message(GameSettings.getInstance()));
                    else
                        out.writeObject(GameCreatorController.creatorMessage.msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Platform.runLater(()-> {
                    try {
                        out.writeObject(new Message(FETCH_AIRPLANES));
                        out.writeObject(new Message(FETCH_CHECKPOINTS));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case STREAM_IDLE:
                //again, nothing has to be done
                break;
        }

    }

    public void sayHello() throws IOException {
        out.writeObject(new Message(Message.msgTypes.CLIENT_HELLO));
    }

    public void sayGoodbye() throws IOException {
        Message message = new Message(GAME_HISTORY_END);
        out.writeObject(message);
        initializeSemaphore.release();
        dataSemaphore.release();
    }

    public void writeMessage(Message msg) throws IOException {
        out.writeObject(msg);
    }


    public void updateIP() throws IOException {
        if(ipAddress.equals(GameSettings.getInstance().ipAddress))
            return;
        setStreamState(StreamStates.STREAM_IDLE);
        out.writeObject(new Message(Message.msgTypes.DISCONNECT));
        socket.close();
        socket = new Socket(GameSettings.getInstance().getIpAddress(), 2137);
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        sayHello();
    }
}
