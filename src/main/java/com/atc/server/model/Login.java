package com.atc.server.model;

import java.io.Serializable;

public class Login implements Serializable {
    private int playerId;
    private String playerLogin;

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
