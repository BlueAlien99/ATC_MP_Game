package com.atc.server;

import com.atc.client.model.Airplane;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Message implements Serializable {

    public static final int CHAT_MESSAGE = 1;
    public static final int AIRPLANE_COMMAND = 2;
    public static final int AIRPLANES_LIST = 3;

    private ConcurrentHashMap<String, Airplane> airplanes;
    private Airplane updatedAirplane;
    private String chatMsg;

    private int msgType;

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
}
