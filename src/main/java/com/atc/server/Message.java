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

public class Message implements Serializable {


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


    public Message(msgTypes m){
        msgType=m;
    }

    public Message(List<Integer> availableGameId){
        this.msgType = msgTypes.GAME_HISTORY;
        this.availableGameId = availableGameId;
    }

    public Message(int gameid){
        this.msgType = msgTypes.GAME_HISTORY;
        this.gameid = gameid;
    }
    public Message(int gameid, List<Event> eventsList, HashMap<UUID, String> Callsigns,
                   HashMap<Integer, String> Logins, List<Checkpoint> dbCheckpoints){
        this.msgType = msgTypes.GAME_HISTORY;
        this.gameid = gameid;
        this.eventsList = eventsList;
        this.Callsigns = Callsigns;
        this.Logins = Logins;
        this.dbCheckpoints = dbCheckpoints;
    }

    public Message(ConcurrentHashMap<UUID, Airplane> airplanes) {
        this.airplanes = airplanes;
        this.msgType = msgTypes.AIRPLANES_LIST;
    }

    public Message(Airplane updatedAirplane) {
        this.updatedAirplane = updatedAirplane;
        this.msgType = msgTypes.AIRPLANE_COMMAND;
    }

    public Message(String chatMsg) {
        this.chatMsg = chatMsg;
        this.msgType = msgTypes.CHAT_MESSAGE;
    }

    public Message(GameSettings gameSettings){
        this.gameSettings = gameSettings;
        this.msgType = msgTypes.CLIENT_SETTINGS;
    }


    public int getGameid() {return gameid;}

    public List<Event> getEventsList() { return eventsList; }

    public msgTypes getMsgType() {
        return msgType;
    }

    public ConcurrentHashMap<UUID, Airplane> getAirplanes() { return airplanes; }

    public void setAirplanes(ConcurrentHashMap<UUID, Airplane> airplanes) {this.airplanes=airplanes;}

    public ConcurrentHashMap<UUID, Checkpoint> getCheckpoints() { return checkpoints;}

    public void setCheckpoints(ConcurrentHashMap<UUID, Checkpoint> checkpoints) {this.checkpoints=checkpoints;}

    public void setSpawnRatio(double spawnRatio) {this.spawnRatio = spawnRatio;}

    public double getSpawnRatio() {return spawnRatio;}

    public Airplane getUpdatedAirplane() {
        return updatedAirplane;
    }

    public String getChatMsg() {
        return chatMsg;
    }

    public GameSettings getGameSettings() {return gameSettings;}
    public void setGameSettings(GameSettings gameSettings) {this.gameSettings = gameSettings;}

    public HashMap<UUID, String> getCallsigns() {
        return Callsigns;
    }

    public HashMap<Integer, String> getLogins() {
        return Logins;
    }

    public List<Integer> getAvailableGameId() {
        return availableGameId;
    }

    public List<Checkpoint> getDbCheckpoints() {
        return dbCheckpoints;
    }

    public List<Pair<UUID, UUID>> getCheckpointsAirplanesMapping() {
        return checkpointsAirplanesMapping;
    }

    public void setCheckpointsAirplanesMapping(Vector<Pair<UUID, UUID>> checkpointsAirplanesMapping) {
        this.checkpointsAirplanesMapping = checkpointsAirplanesMapping;
    }

    public List<Player> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(List<Player> playersList) {
        this.playersList = playersList;
    }

    public void setLogins(HashMap<Integer, String> logins) {
        Logins = logins;
    }

    public List<Login> getBestScoresLoginList() {
        return bestScoresLoginList;
    }

    public void setBestScoresLoginList(List<Login> bestScoresLoginList) {
        this.bestScoresLoginList = bestScoresLoginList;
    }
}
