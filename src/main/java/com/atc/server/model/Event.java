package com.atc.server.model;
import java.io.Serializable;
import java.util.UUID;

public class Event implements Serializable {
    public enum eventType {
        COLLISION, COMMAND,
        CHECKPOINT, MOVEMENT
    }
    private int gameId;
    private int eventId;
    private eventType type;
    private int timeTick;
    private int playerId;
    private double xCoordinate;
    private double yCoordinate;
    private double speed;
    private double heading;
    private double height;
    private UUID airplaneUUID;


    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

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

    public UUID getAirplaneUUID() {
        return airplaneUUID;
    }

    public void setAirplaneUUID(UUID airplaneUUID) {
        this.airplaneUUID = airplaneUUID;
    }

    public Event( int eventId, int gameId, eventType type, int timeTick, int playerId, double xCoordinate, double yCoordinate, double speed, double heading, double height, UUID airplaneUUID) {
        this.gameId = gameId;
        this.eventId = eventId;
        this.type = type;
        this.timeTick = timeTick;
        this.playerId = playerId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.speed = speed;
        this.heading = heading;
        this.height = height;
        this.airplaneUUID = airplaneUUID;
    }

    @Override
    public String toString() {
        String message = "GAME: " + gameId+ ' ' + eventId + " ";
        message += "TICK: "+ String.valueOf(timeTick);
        message += " " + type;
        message += "["+ xCoordinate+"," + yCoordinate + "] ";
        message += "Speed: " + speed + ", Heading: " + heading + ", Height: " + height;
        message += " " + airplaneUUID +":";
        message += "[PLAYER] " + playerId;
        return message;
    }
}
