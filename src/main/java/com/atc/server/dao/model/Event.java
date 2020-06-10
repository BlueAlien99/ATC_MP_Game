package com.atc.server.dao.model;
import java.io.Serializable;
import java.util.UUID;

public class Event implements Serializable {
    public enum eventType {
        COLLISION, COMMAND,
        CHECKPOINT, MOVEMENT, OFFTHEBOARD
    }
    private int gameId;
    private int eventId;
    private eventType type;
    private int timeTick;
    private int playerId;
    private int points;
    private double xCoordinate;
    private double yCoordinate;
    private double speed;
    private double heading;
    private double height;
    private UUID airplaneUUID;

    public int getPoints() {
        return points;
    }

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


    public eventType getType() {
        return type;
    }

    public int getTimeTick() {
        return timeTick;
    }

    public int getPlayerId() {
        return playerId;
    }

    public double getxCoordinate() {
        return xCoordinate;
    }

    public double getyCoordinate() {
        return yCoordinate;
    }

    public UUID getAirplaneUUID() {
        return airplaneUUID;
    }

    public Event( int eventId, int gameId, eventType type, int timeTick, int playerId, int points,
                  double xCoordinate, double yCoordinate, double speed, double heading, double height, UUID airplaneUUID) {
        this.gameId = gameId;
        this.eventId = eventId;
        this.type = type;
        this.timeTick = timeTick;
        this.playerId = playerId;
        this.points = points;
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
        message += "TICK: "+ timeTick;
        message += " " + type;
        message += "["+ xCoordinate+"," + yCoordinate + "] ";
        message += "Speed: " + speed + ", Heading: " + heading + ", Height: " + height;
        message += " " + airplaneUUID +":";
        message += "[PLAYER] " + playerId;
        return message;
    }
}
