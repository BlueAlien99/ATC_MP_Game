package com.atc.server.model;


import java.util.UUID;

public class Event {
    public enum eventType {
        COLLISION, COMMAND,
        CHECKPOINT, MOVEMENT
    }
    private int eventId;
    private eventType type;
    private int timeTick;
    private int playerId;
    private double xCoordinate;
    private double yCoordinate;
    private UUID airplaneId;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }


    public eventType getType() {
        return type;
    }

    public void setType(eventType type) {
        this.type = type;
    }

    public int getTimeTick() {
        return timeTick;
    }

    public void setTimeTick(int timeTick) {
        this.timeTick = timeTick;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public double getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public double getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public UUID getAirplaneId() {
        return airplaneId;
    }

    public void setAirplaneId(UUID airplaneId) {
        this.airplaneId = airplaneId;
    }

    public Event(int eventId, eventType type, int timeTick, int playerId, double xCoordinate, double yCoordinate, UUID airplaneId) {
        this.eventId = eventId;
        this.type = type;
        this.timeTick = timeTick;
        this.playerId = playerId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.airplaneId = airplaneId;
    }

    @Override
    public String toString() {
        String message = eventId + " ";
        message += String.valueOf(timeTick);
        message += " " + type;
        message += "["+ xCoordinate+"," + yCoordinate + "]";
        message += " " + airplaneId +":";
        message += playerId;
        return message;
    }
}
