package com.atc.server;

import com.atc.client.model.Airplane;
import com.atc.client.model.GameSettings;
import com.atc.server.model.Event;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Message implements Serializable {

    public static final int CHAT_MESSAGE = 1;
    public static final int AIRPLANE_COMMAND = 2;
    public static final int AIRPLANES_LIST = 3;
    public static final int CLIENT_HELLO = 4;
    public static final int EVENTS_LIST = 5;

    private int gameid;
    List<Event> eventsList;
    private ConcurrentHashMap<String, Airplane> airplanes;
    private Airplane updatedAirplane;
    private String chatMsg;
    private GameSettings gameSettings;

    private int msgType;

    public Message(int gameid){
        this.gameid = gameid;
    }
    public Message(int gameid, List<Event> eventsList){
        this.gameid = gameid;
        this.eventsList = eventsList;
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
        this.msgType = CLIENT_HELLO;
    }

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
}
