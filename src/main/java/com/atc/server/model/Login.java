package com.atc.server.model;

import java.io.Serializable;

public class Login implements Serializable {
    private int gameID;
    private int playerId;
    private String playerLogin;

    public Login(int gameID, int playerId, String playerLogin) {
        this.gameID = gameID;
        this.playerId = playerId;
        this.playerLogin = playerLogin;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getPlayerLogin() {
        return playerLogin;
    }

    public void setPlayerLogin(String playerLogin) {
        this.playerLogin = playerLogin;
    }
}
