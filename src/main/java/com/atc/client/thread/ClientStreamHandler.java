package com.atc.client.thread;

import com.atc.client.controller.GameCreatorController;
import com.atc.client.model.Checkpoint;
import com.atc.client.model.GameActivity;
import com.atc.client.model.GameSettings;
import com.atc.server.model.Message;
import com.atc.server.dao.model.Event;
import com.atc.server.dao.model.Login;
import com.atc.server.dao.model.Player;
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

import static com.atc.server.model.Message.msgTypes.*;

/**
 * Class that enables communication between the server, client and database.
 */

public class ClientStreamHandler implements Runnable {
    public static ClientStreamHandler singleton;
    public static Thread thread;

    private static String ipAddress = "localhost";

    /**
     * ClientStreamHandler is a singleton, that means there can be only one object of this class,
     * and this method permits to obtain it.
     * @return ClientStremHandler object
     */

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

    /**
     * Interrupts thread inside class.
     */

    public void interrupt(){
        thread.interrupt();
    }

    /**
     * Sets chatBox
     * @param chatBox chatBox VBox
     */
    public void setChatBox(VBox chatBox) {
        this.chatBox = chatBox;
    }

    /**
     * Sets gameActivity obejct.
     * @param gameActivity gameActivity
     */
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
    public Semaphore dataSemaphore = new Semaphore(0,true);
    private Semaphore initializeSemaphore = new Semaphore(0, true);

    /**
     * Gets list of Events
     * @return list of Events
     */

    public List<Event> getEvents() {
        return Events;
    }

    /**
     * Gets list of checkpoints.
     * @return list of checkpoints
     */
    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    /**
     * Sets searched gameID so we could tell database which replay we want to get.
     * @param searchedGameId searched gameID
     */
    public void setSearchedGameId(int searchedGameId) {
        this.searchedGameId = searchedGameId;
    }

    /**
     * Gets hashmap of airplanes' callsigns and their UUIDs
     * @return hashmap of UUIDs and Callsigns
     */
    public HashMap<UUID, String> getCallsigns(){ return Callsigns;}

    /**
     * Gets hashmap of players' logins.
     * @return hashmap of players' logins.
     */
    public HashMap<Integer, String> getLogins() {return Logins;}

    /**
     * Gets a list of available replays from database
     * @return list of available replays
     */
    public List<Integer> getAvailableGames() { return availableGames; }

    /**
     * Gets list of players
     * @return list of players
     */
    public List<Player> getPlayersList() {
        return playersList;
    }

    /**
     * Gets list of Logins that will be displayed in Best Scores.
     * @return list of logins
     */
    public List<Login> getBestScoresLoginsList() {
        return bestScoresLoginsList;
    }



    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public enum StreamStates {STREAM_HISTORY, STREAM_GAME, STREAM_IDLE}
    private StreamStates streamState = StreamStates.STREAM_IDLE;


    public boolean connected = false;
    private boolean terminated = false;

    private Message.msgTypes lastMsgType;

    @Override
    public void run() {
        try {
            //System.out.println(GameSettings.getInstance().getClientUUID().toString());
            socket = new Socket("localhost", 2137);
            //System.out.println("Connected!");
            //System.out.println(socket.toString());
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            //System.out.println("Unable to connect. Are you sure about IP address?");
        }

        if(in==null || out==null || socket.isClosed()) {
            streamNotifier.release();
            return;
        }
        streamNotifier.release();
        connected=true;

        while (connected && !Thread.currentThread().isInterrupted()) {
            Message msg = null;
            try {
                Object og = in.readObject();
                System.out.println(og);
                msg = (Message) og;
            } catch (IOException e) {
                resetConnection();
            }
            catch (ClassNotFoundException e){
                e.printStackTrace();
            }
            if (msg == null)
                continue;
            lastMsgType = msg.getMsgType();
            if(lastMsgType==DISCONNECT){
                try {
                    disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
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

    /**
     * Forces client connection with local server.
     * @throws IOException exception
     */

    private void disconnect() throws IOException {
        if(!socket.isClosed())
            socket.close();
        try{
            updateIP();
        }
        catch (IOException e){
            GameSettings.getInstance().setIpAddress("localhost");
            updateIP();
        }
    }

    /**
     * Manages stream operation when server is in idle state.
     * @param msg last message sent to server
     */

    private void idleSwitch(Message msg){
        if (lastMsgType == DISCONNECT) {
            GameSettings.getInstance().setIpAddress("localhost");
            try {
                updateIP();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manages stream operation when server is in history state.
     * <br>
     *     GAME_HISTORY - searchedGameID>=0 client asks for events about corresponding game from the database, otherwise he asks about available replays
     *<br>
     *     PLAYERS_LIST - client wants to know best scores of players
     * <br>
     *     GAME_HISTORY_END - client don't want to exchange data with the database anymore
     * @param msg last message sent to server
     * @throws IOException exception
     */

    private void historySwitch(Message msg) throws IOException {
        switch (lastMsgType){
            case GAME_HISTORY:
                if(searchedGameId >= 0){
                    //System.out.println("Received Events list from server!");
                    Events = msg.getEventsList();
                    Callsigns = msg.getCallsigns();
                    Logins = msg.getLogins();
                    checkpoints = msg.getDbCheckpoints();
                    dataSemaphore.release();
                    System.out.println("dataSemaphore released1!");
                }
                else{
                    //System.out.println("Received available replays from server!");
                    availableGames = msg.getAvailableGameId();
                    dataSemaphore.release();
                    System.out.println("dataSemaphore released2!");
                }
                break;
            case PLAYERS_LIST:
                playersList = msg.getPlayersList();
                bestScoresLoginsList = msg.getBestScoresLoginList();
                dataSemaphore.release();
                System.out.println("dataSemaphore released3!");
                break;
            case GAME_HISTORY_END:
                //System.out.println("End of exchanging data with server");
                setStreamState(StreamStates.STREAM_IDLE);
                break;
        }
    }

    /**
     *  Manages stream operation when server is in game state.
     * <br>
     *     AIRPLANES_LIST - message sent to client every tick to update his list of airplanes
     * <br>
     *     CHECKPOINTS_LIST - message to client with list of checkpoint, when something related to checkpoints happened in game
     * <br>
     *     CHAT_MESSAGE - message with label to display on player's chatBox (usually it's a comment about event that happened in game)
     * <br>
     *     SERVER_GOODBYE - ends communication with server
     * @param msg last message sent to server
     * @throws IOException exception
     */

    private void gameSwitch(Message msg) throws IOException {
        switch (lastMsgType) {
            case AIRPLANES_LIST:
                gameActivity.updateAirplanes(msg.getAirplanes());
                Platform.runLater(
                        () -> gameActivity.wrapPrinting());
                //msg.getAirplanes().forEach((k,v)-> System.out.println(v.getCallsign()+" "+v.getPosX()+" "+v.getPosY()));
                //System.out.println("Got airplanes");
                break;
            case CHECKPOINTS_LIST:
                ConcurrentHashMap<UUID, Checkpoint> tempMap = msg.getCheckpoints();
                msg.getCheckpointsAirplanesMapping().forEach(pair ->
                        tempMap.get(pair.getKey()).passAirplane(pair.getValue()));
                gameActivity.setCheckpoints(tempMap);
                Platform.runLater(
                        () -> gameActivity.wrapPrinting());
                //System.out.println("Got checkpoints");
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

    /**
     * Sends request to server for players' logins and other parameters needed in BestSCoreController.
     * @throws InterruptedException exception
     * @throws IOException exception
     */
    public void askForPlayers() throws InterruptedException, IOException {
        initializeSemaphore.acquire();
        Message message = new Message(FETCH_PLAYERS);
        out.writeObject(message);
        //System.out.println("Requested data!");
        initializeSemaphore.release();
        dataSemaphore.acquire();
        System.out.println("dataSemaphore acquired1!");
    }

    /**
     * Used in GameHistoryController to either ask for available replays or get events from certain game.
     * @throws InterruptedException exception
     * @throws IOException exception
     */

    public void sendRequestForData() throws InterruptedException, IOException {
        initializeSemaphore.acquire();
        Message message = new Message(searchedGameId);
        out.writeObject(message);
        System.out.println("Requested data!");
        initializeSemaphore.release();
        dataSemaphore.acquire();
        System.out.println("dataSemaphore acquired2!");
    }

    /**
     * Resets connection with current server
     */
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

    /**
     * Sets stream to given state
     * @param newState - new state for stream
     * @throws IOException exception
     */
    public void setStreamState(StreamStates newState) throws IOException {
        System.out.println("setStreamState "+streamState+" to "+newState);
        if(streamState==newState)
            return;
        switch (streamState){
            case STREAM_HISTORY:
                out.writeObject(new Message(Message.msgTypes.CLIENT_GOODBYE));
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

    /**
     * Sends message that initialize communication with server
     * @throws IOException exception
     */
    public void sayHello() throws IOException {
        out.writeObject(new Message(Message.msgTypes.CLIENT_HELLO));
    }

    /**
     * Sends message that ends communication with server
     * @throws IOException exception
     */
    public void sayGoodbye() throws IOException {
        Message message = new Message(GAME_HISTORY_END);
        out.writeObject(message);
        initializeSemaphore.release();
        dataSemaphore.release();
        System.out.println("dataSemaphore released4!");
    }

    /**
     * Writes message to stream so it can be transported to server
     * @param msg - message to write
     * @throws IOException exception
     */
    public void writeMessage(Message msg) throws IOException {
        out.writeObject(msg);
    }

    /**
     * Changes stream's IP address, so it can communicate with a remote server, closes communication with old one
     * and then initializes contact with new.
     * @throws IOException exception
     */
    public void updateIP() throws IOException {
        System.out.println("UpdateIP" + streamState);
        if(ipAddress.equals(GameSettings.getInstance().getIpAddress()))
            return;
        System.out.println("UpdateIP continued");
        setStreamState(StreamStates.STREAM_IDLE);
        out.writeObject(new Message(Message.msgTypes.DISCONNECT));
        socket.close();
        socket = new Socket(GameSettings.getInstance().getIpAddress(), 2137);
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        sayHello();
    }
}
