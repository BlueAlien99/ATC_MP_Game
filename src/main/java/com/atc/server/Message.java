package com.atc.server;

import com.atc.client.model.Airplane;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {

    private ArrayList<Airplane> airplanes;
    private Airplane updatedAirplane;
    private String chatMsg;

    public Message(ArrayList<Airplane> airplanes) {
        this.airplanes = airplanes;
    }

    public Message(Airplane updatedAirplane) {
        this.updatedAirplane = updatedAirplane;
    }

    public Message(String chatMsg) {
        this.chatMsg = chatMsg;
    }
}
