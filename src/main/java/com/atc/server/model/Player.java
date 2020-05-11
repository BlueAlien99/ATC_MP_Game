package com.atc.server.model;

public class Player {
    int playerId;
    String playerName;
    int points;
    int airplanesNum;
    double timeInGame;

    public int getIdPlayer() {
        return playerId;
    }

    public void setIdPlayer(int idPlayer) {
        this.playerId = idPlayer;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getAirplanesNum() {
        return airplanesNum;
    }

    public void setAirplanesNum(int airplanesNum) {
        this.airplanesNum = airplanesNum;
    }

    public double getTimeInGame() {
        return timeInGame;
    }

    public void setTimeInGame(double timeInGame) {
        this.timeInGame = timeInGame;
    }

    @Override
    public String toString() {
        String player = "["+ playerId + "]";
        player += " " + playerName;
        player += "(" + points +",";
        player += " " + airplanesNum + ",";
        player += " " + timeInGame + ")";
        return player;
    }

    public Player(int playerId, String playerName, int points, int airplanesNum, double timeInGame) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.points = points;
        this.airplanesNum = airplanesNum;
        this.timeInGame = timeInGame;
    }
}
