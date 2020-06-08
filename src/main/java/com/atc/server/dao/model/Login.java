package com.atc.server.dao.model;

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

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerLogin() {
        return playerLogin;
    }

}
