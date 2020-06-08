package com.atc.server.dao.model;

import java.io.Serializable;
import java.util.UUID;

public class Player implements Serializable {
    int playerId;
    UUID playerUUID;
    int points;
    int airplanesNum;
    double timeInGame;

    public int getIdPlayer() {
        return playerId;
    }

    public int getPoints() {
        return points;
    }

    public int getAirplanesNum() {
        return airplanesNum;
    }

    @Override
    public String toString() {
        String player = "["+ playerId + "]";
        player += " " + playerUUID.toString();
        player += "(" + points +",";
        player += " " + airplanesNum + ",";
        player += " " + timeInGame + ")";
        return player;
    }

    public Player(int playerId, UUID playerName, int points, int airplanesNum, double timeInGame) {
        this.playerId = playerId;
        this.playerUUID = playerName;
        this.points = points;
        this.airplanesNum = airplanesNum;
        this.timeInGame = timeInGame;
    }
}
