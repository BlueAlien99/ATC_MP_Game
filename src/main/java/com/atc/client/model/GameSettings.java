package com.atc.client.model;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container singleton class with settings needed in establishing new game.
 */

public class GameSettings implements Serializable {

    private static GameSettings singleton;
    public static final int SINGLE_PLAYER = 0;

    String ipAddress = "localhost";
    int planeNum = 3;
    UUID clientUUID;
    String clientName = "Player";
    ConcurrentHashMap<UUID, Airplane> planesToSpawn;
    int gameMode = SINGLE_PLAYER;

    public synchronized static GameSettings getInstance() {
        if (singleton == null) {
            singleton = new GameSettings();
        }
        return singleton;
    }

    public String getIpAddress(){
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPlaneNum() {
        return planeNum;
    }

    public void setPlaneNum(int planeNum) {
        this.planeNum = planeNum;
    }

    public ConcurrentHashMap<UUID, Airplane> getPlanesToSpawn() {
        return planesToSpawn;
    }

    public void setPlanesToSpawn(ConcurrentHashMap<UUID, Airplane> planesToSpawn) {
        this.planesToSpawn = planesToSpawn;
    }

    public UUID getClientUUID() {
        return clientUUID;
    }

    public void setClientUUID(UUID clientUUID) {
        this.clientUUID = clientUUID;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    private GameSettings(){
        clientUUID = UUID.randomUUID();
        planesToSpawn = null;
    }

}
