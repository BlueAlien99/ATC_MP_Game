package com.atc.server;

import com.atc.client.model.Airplane;
import com.atc.client.model.Checkpoint;
import com.atc.client.model.GameSettings;
import com.atc.server.model.Event;
import com.atc.server.model.Login;
import com.atc.server.model.Player;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class of messages that server and client can send to each other to enable communication, data exchange etc.
 */
public class Message implements Serializable {

    /**
     * There were many types of messages and there was not enough primitive types to create all constructors,
     * so we decided to create enum msgTypes so messages could be differentiated without having to create
     * dummy constructors such as Message(char c).
     */
    public enum msgTypes{
        CHAT_MESSAGE,  AIRPLANE_COMMAND,  AIRPLANES_LIST,  CLIENT_HELLO,  CLIENT_SETTINGS,  GAME_HISTORY,
        GAME_HISTORY_END,  GAME_PAUSE,  GAME_RESUME,  CLIENT_GOODBYE,  SERVER_GOODBYE,  FETCH_AIRPLANES,
        SEND_INITIAL, NEW_GAME, FETCH_CHECKPOINTS, CHECKPOINTS_LIST, FETCH_PLAYERS, PLAYERS_LIST,
        DISCONNECT
    }

    private int gameid;
    List<Player> playersList;
    List<Checkpoint> dbCheckpoints;
    List<Event> eventsList;
    List<Integer> availableGameId;
    private ConcurrentHashMap<UUID, Airplane> airplanes;
    private ConcurrentHashMap<UUID, Checkpoint> checkpoints;
    private double spawnRatio;
    private HashMap<UUID, String> Callsigns;
    private HashMap<Integer, String> Logins;
    private List<Login> bestScoresLoginList;
    private Airplane updatedAirplane;
    private String chatMsg;
    private GameSettings gameSettings;
    private Vector<Pair<UUID, UUID>> checkpointsAirplanesMapping;

    private msgTypes msgType;

    /**
     * Default constructor for Message.
     * @param m type of msg (enum msgTypes)
     */
    public Message(msgTypes m){
        msgType=m;
    }

    /**
     * Instantiates a new Message.
     */
    public Message(){
        this.msgType = msgTypes.CLIENT_HELLO;
    }

    /**
     * Constructor of message passing list of available game replays
     *
     * @param availableGameId List of replays in database
     */
    public Message(List<Integer> availableGameId){
        this.msgType = msgTypes.GAME_HISTORY;
        this.availableGameId = availableGameId;
    }

    /**
     * Constructor of message passing ID of game we want ot see the replay.
     *
     * @param gameid the gameid
     */
    public Message(int gameid){
        this.msgType = msgTypes.GAME_HISTORY;
        this.gameid = gameid;
    }

    /**
     * Constructor of Message passing data about game from database to client.
     *
     * @param gameid        the gameid
     * @param eventsList    the events list
     * @param Callsigns     the callsigns
     * @param Logins        the logins
     * @param dbCheckpoints the db checkpoints
     */
    public Message(int gameid, List<Event> eventsList, HashMap<UUID, String> Callsigns,
                   HashMap<Integer, String> Logins, List<Checkpoint> dbCheckpoints){
        this.msgType = msgTypes.GAME_HISTORY;
        this.gameid = gameid;
        this.eventsList = eventsList;
        this.Callsigns = Callsigns;
        this.Logins = Logins;
        this.dbCheckpoints = dbCheckpoints;
    }

    /**
     * Constructor of Message passing list of airplanes from the database .
     *
     * @param airplanes the airplanes
     */
    public Message(ConcurrentHashMap<UUID, Airplane> airplanes) {
        this.airplanes = airplanes;
        this.msgType = msgTypes.AIRPLANES_LIST;
    }

    /**
     * Constructor of Message passing updated airplane to client.
     *
     * @param updatedAirplane the updated airplane
     */
    public Message(Airplane updatedAirplane) {
        this.updatedAirplane = updatedAirplane;
        this.msgType = msgTypes.AIRPLANE_COMMAND;
    }

    /**
     * Constructor of Message passing a chat message of user.
     *
     * @param chatMsg the chat msg
     */
    public Message(String chatMsg) {
        this.chatMsg = chatMsg;
        this.msgType = msgTypes.CHAT_MESSAGE;
    }

    /**
     * Constructor of Message passing gameSettings to server.
     *
     * @param gameSettings the game settings
     */
    public Message(GameSettings gameSettings){
        this.gameSettings = gameSettings;
        this.msgType = msgTypes.CLIENT_SETTINGS;
    }


    /**
     * Gets gameid.
     *
     * @return the gameid
     */
    public int getGameid() {return gameid;}

    /**
     * Gets events list.
     *
     * @return the events list
     */
    public List<Event> getEventsList() { return eventsList; }

    /**
     * Gets msg type.
     *
     * @return the msg type
     */
    public msgTypes getMsgType() {
        return msgType;
    }

    /**
     * Gets airplanes.
     *
     * @return the airplanes
     */
    public ConcurrentHashMap<UUID, Airplane> getAirplanes() { return airplanes; }

    /**
     * Sets airplanes.
     *
     * @param airplanes the airplanes
     */
    public void setAirplanes(ConcurrentHashMap<UUID, Airplane> airplanes) {this.airplanes=airplanes;}

    /**
     * Gets checkpoints.
     *
     * @return the checkpoints
     */
    public ConcurrentHashMap<UUID, Checkpoint> getCheckpoints() { return checkpoints;}

    /**
     * Sets checkpoints.
     *
     * @param checkpoints the checkpoints
     */
    public void setCheckpoints(ConcurrentHashMap<UUID, Checkpoint> checkpoints) {this.checkpoints=checkpoints;}

    /**
     * Sets spawn ratio.
     *
     * @param spawnRatio the spawn ratio
     */
    public void setSpawnRatio(double spawnRatio) {this.spawnRatio = spawnRatio;}

    /**
     * Gets spawn ratio.
     *
     * @return the spawn ratio
     */
    public double getSpawnRatio() {return spawnRatio;}

    /**
     * Gets updated airplane.
     *
     * @return the updated airplane
     */
    public Airplane getUpdatedAirplane() {
        return updatedAirplane;
    }

    /**
     * Gets chat msg.
     *
     * @return the chat msg
     */
    public String getChatMsg() {
        return chatMsg;
    }

    /**
     * Gets game settings.
     *
     * @return the game settings
     */
    public GameSettings getGameSettings() {return gameSettings;}

    /**
     * Sets game settings.
     *
     * @param gameSettings the game settings
     */
    public void setGameSettings(GameSettings gameSettings) {this.gameSettings = gameSettings;}

    /**
     * Gets callsigns.
     *
     * @return the callsigns
     */
    public HashMap<UUID, String> getCallsigns() {
        return Callsigns;
    }

    /**
     * Gets logins.
     *
     * @return the logins
     */
    public HashMap<Integer, String> getLogins() {
        return Logins;
    }

    /**
     * Gets available game id.
     *
     * @return the available game id
     */
    public List<Integer> getAvailableGameId() {
        return availableGameId;
    }

    /**
     * Gets db checkpoints.
     *
     * @return the db checkpoints
     */
    public List<Checkpoint> getDbCheckpoints() {
        return dbCheckpoints;
    }

    /**
     * Gets checkpoints airplanes mapping.
     *
     * @return the checkpoints airplanes mapping
     */
    public List<Pair<UUID, UUID>> getCheckpointsAirplanesMapping() {
        return checkpointsAirplanesMapping;
    }

    /**
     * Sets checkpoints airplanes mapping.
     *
     * @param checkpointsAirplanesMapping the checkpoints airplanes mapping
     */
    public void setCheckpointsAirplanesMapping(Vector<Pair<UUID, UUID>> checkpointsAirplanesMapping) {
        this.checkpointsAirplanesMapping = checkpointsAirplanesMapping;
    }

    /**
     * Gets players list.
     *
     * @return the players list
     */
    public List<Player> getPlayersList() {
        return playersList;
    }

    /**
     * Sets players list.
     *
     * @param playersList the players list
     */
    public void setPlayersList(List<Player> playersList) {
        this.playersList = playersList;
    }

    /**
     * Sets logins.
     *
     * @param logins the logins
     */
    public void setLogins(HashMap<Integer, String> logins) {
        Logins = logins;
    }

    /**
     * Gets best scores login list.
     *
     * @return the best scores login list
     */
    public List<Login> getBestScoresLoginList() {
        return bestScoresLoginList;
    }

    /**
     * Sets best scores login list.
     *
     * @param bestScoresLoginList the best scores login list
     */
    public void setBestScoresLoginList(List<Login> bestScoresLoginList) {
        this.bestScoresLoginList = bestScoresLoginList;
    }
}
