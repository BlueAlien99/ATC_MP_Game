package com.atc.server.model;

import java.io.Serializable;
import java.util.UUID;

public class Callsign implements Serializable {
    private UUID airplaneUUID;
    private String airplaneCallsign;

    public UUID getAirplaneUUID() {
        return airplaneUUID;
    }

    public void setAirplaneUUID(UUID airplaneUUID) {
        this.airplaneUUID = airplaneUUID;
    }

    public String getAirplaneCallsign() {
        return airplaneCallsign;
    }

    public void setAirplaneCallsign(String airplaneCallsign) {
        this.airplaneCallsign = airplaneCallsign;
    }
}
