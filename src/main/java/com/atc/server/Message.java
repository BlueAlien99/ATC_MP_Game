package com.atc.server;

import com.atc.client.model.Airplane;
import com.atc.client.model.GameHistory;
import com.atc.client.model.GameSettings;
import com.atc.server.model.Event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Message implements Serializable {

    public static final int CHAT_MESSAGE = 1;
    public static final int AIRPLANE_COMMAND = 2;
    public static final int AIRPLANES_LIST = 3;
    public static final int CLIENT_HELLO = 4;
    public static final int CLIENT_SETTINGS = 5;
    public static final int GAME_HISTORY = 6;
    public static final int GAME_HISTORY_END = 7;

    private int gameid;
    List<Event> eventsList;
    List<Integer> availableGameId;
    private ConcurrentHashMap<String, Airplane> airplanes;
    private HashMap<UUID, String> Callsigns;
    private HashMap<Integer, String> Logins;
    private Airplane updatedAirplane;
    private String chatMsg;
    private GameSettings gameSettings;

    private int msgType;

    public Message(){
        this.msgType = CLIENT_HELLO;
    }
    public Message(char c){this.msgType = GAME_HISTORY_END;}

    public Message(List<Integer> availableGameId){
        this.msgType = GAME_HISTORY;
        this.availableGameId = availableGameId;
    }

    public Message(int gameid){
        this.msgType = GAME_HISTORY;
        this.gameid = gameid;
    }
    public Message(int gameid, List<Event> eventsList, HashMap<UUID, String> Callsigns,
                   HashMap<Integer, String> Logins){
        this.msgType = GAME_HISTORY;
        this.gameid = gameid;
        this.eventsList = eventsList;
        this.Callsigns = Callsigns;
        this.Logins = Logins;
    }

    public Message(ConcurrentHashMap<String, Airplane> airplanes) {
        this.airplanes = airplanes;
        this.msgType = AIRPLANES_LIST;
    }

    public Message(Airplane updatedAirplane) {
        this.updatedAirplane = updatedAirplane;
        this.msgType = AIRPLANE_COMMAND;
    }

    public Message(String chatMsg) {
        this.chatMsg = chatMsg;
        this.msgType = CHAT_MESSAGE;
    }

    public Message(GameSettings gameSettings){
        this.gameSettings = gameSettings;
        this.msgType = CLIENT_SETTINGS;
    }

    public int getGameid() {return gameid;}

    public List<Event> getEventsList() { return eventsList; }

    public int getMsgType() {
        return msgType;
    }

    public ConcurrentHashMap<String, Airplane> getAirplanes() {
        return airplanes;
    }

    public Airplane getUpdatedAirplane() {
        return updatedAirplane;
    }

    public String getChatMsg() {
        return chatMsg;
    }

    public GameSettings getGameSettings() {return gameSettings;}

    public HashMap<UUID, String> getCallsigns() {
        return Callsigns;
    }

    public HashMap<Integer, String> getLogins() {
        return Logins;
    }

    public List<Integer> getAvailableGameId() {
        return availableGameId;
    }
}
