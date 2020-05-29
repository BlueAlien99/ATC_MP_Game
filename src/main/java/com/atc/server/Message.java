package com.atc.server;

import com.atc.client.model.Airplane;
import com.atc.client.model.GameSettings;
import com.atc.server.model.Event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Message implements Serializable {

    public enum msgTypes{
        CHAT_MESSAGE,  AIRPLANE_COMMAND,  AIRPLANES_LIST,  CLIENT_HELLO,  CLIENT_SETTINGS,  GAME_HISTORY,
        GAME_HISTORY_END,  GAME_PAUSE,  GAME_RESUME,  CLIENT_GOODBYE,  SERVER_GOODBYE,  FETCH_AIRPLANES
    }

    private int gameid;
    List<Event> eventsList;
    List<Integer> availableGameId;
    private ConcurrentHashMap<String, Airplane> airplanes;
    private HashMap<UUID, String> Callsigns;
    private HashMap<Integer, String> Logins;
    private Airplane updatedAirplane;
    private String chatMsg;
    private GameSettings gameSettings;

    private msgTypes msgType;


    //TODO: ENUM-erize constructors
    public Message(msgTypes m){
        msgType=m;
    }

    public Message(){
        this.msgType = msgTypes.CLIENT_HELLO;
    }
    public Message(char c){this.msgType = msgTypes.GAME_HISTORY_END;}

    public Message(List<Integer> availableGameId){
        this.msgType = msgTypes.GAME_HISTORY;
        this.availableGameId = availableGameId;
    }

    public Message(int gameid){
        this.msgType = msgTypes.GAME_HISTORY;
        this.gameid = gameid;
    }
    public Message(int gameid, List<Event> eventsList, HashMap<UUID, String> Callsigns,
                   HashMap<Integer, String> Logins){
        this.msgType = msgTypes.GAME_HISTORY;
        this.gameid = gameid;
        this.eventsList = eventsList;
        this.Callsigns = Callsigns;
        this.Logins = Logins;
    }

    public Message(ConcurrentHashMap<String, Airplane> airplanes) {
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
