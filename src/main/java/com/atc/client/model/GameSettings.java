package com.atc.client.model;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameSettings implements Serializable {
    public static final int SINGLE_PLAYER = 0;
    public static final int MULTI_NONPC = 1;
    public static final int MULTI_WNPC = 2;


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

    String ipAddress = "localhost";
    int planeNum = 0;
    UUID clientUUID;
    String clientName = "Player";
    ConcurrentHashMap<UUID, Airplane> planesToSpawn;
    int gameMode = SINGLE_PLAYER;

    public GameSettings(){
        clientUUID = UUID.randomUUID();
        planesToSpawn = null;
    }

}
