package com.atc.server.gamelog;

import com.atc.server.model.Event;
import com.atc.server.model.Player;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class gameLog {
    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:gamelog.db";

    private Connection con;
    private Statement stat;

    public gameLog(){
        try {
            Class.forName(gameLog.DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: missing JDBC driver.");
            e.printStackTrace();
        }

        try {
            con = DriverManager.getConnection(DB_URL);
            con.createStatement().execute("PRAGMA foreign_keys = ON");
            stat = con.createStatement();
        } catch (SQLException e) {
            System.err.println("ERROR: cannot open connection.");
            e.printStackTrace();
        }

        createTables();
    }

    private boolean createTables(){
        String createPlayers = "CREATE TABLE IF NOT EXISTS PLAYERS" +
                "(PLAYER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PLAYER_NAME VARCHAR(255) NOT NULL," +
                "POINTS INTEGER NOT NULL DEFAULT 0," +
                "AIRPLANES_NUM INTEGER NOT NULL DEFAULT 0," +
                "TIME_IN_GAME DOUBLE DEFAULT 0)";
        String createEvents = "CREATE TABLE IF NOT EXISTS EVENTS" +
                "(EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "EVENT_TYPE VARCHAR(255) NOT NULL," +
                "TICK_TIME INTEGER NOT NULL," +
                "PLAYER_ID INTEGER NOT NULL," +
                "X_COOR DOUBLE NOT NULL," +
                "Y_COOR DOUBLE NOT NULL," +
                "AIRPLANE_ID VARCHAR(255) NOT NULL," +
                "FOREIGN KEY(PLAYER_ID) REFERENCES PLAYERS(PLAYER_ID))";
        try {
            stat.execute(createPlayers);
            stat.execute(createEvents);
        } catch (SQLException e) {
            System.err.println("ERROR: Cannot create tables");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertPlayer(String playerName, int points, int airplanesNum, double timeInGame){
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO PLAYERS VALUES(NULL,?,?,?,?);");
            prepStmt.setString(1,playerName);
            prepStmt.setInt(2,points);
            prepStmt.setInt(3, airplanesNum);
            prepStmt.setDouble(4,timeInGame);
            prepStmt.execute();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add player:" + playerName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertEvent(String eventType, int timeTick, int playerId, double xCoordinate, double yCoordinate,
                               String airplaneId){
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO EVENTS VALUES(NULL,?,?,?,?,?,?);");
            prepStmt.setString(1,eventType);
            prepStmt.setInt(2, timeTick);
            prepStmt.setInt(3, playerId);
            prepStmt.setDouble(4, xCoordinate);
            prepStmt.setDouble(5, yCoordinate);
            prepStmt.setString(6, airplaneId);
            prepStmt.execute();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add event:" + eventType + ":" + timeTick);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Event> selectAllEvents(){
        List<Event> Events;
        try {
            ResultSet result = stat.executeQuery("SELECT * FROM EVENTS");
            Events = getEventResults(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Events;
    }

    public List<Player> selectAllPlayers(){
        List<Player> Players;
        try {
            ResultSet result = stat.executeQuery("SELECT * FROM PLAYERS");
            Players = getPlayersResult(result);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Players;
    }

    private List<Player> getPlayersResult(ResultSet result){
        List<Player> Players = new LinkedList<>();
        try{
            int id, points, airplanesNum;
            double timeInGame;
            String playerName;
            while(result.next()) {
                id = result.getInt("player_id");
                playerName = result.getString("player_name");
                points = result.getInt("points");
                airplanesNum = result.getInt("airplanes_num");
                timeInGame = result.getDouble("time_in_game");
                Players.add(new Player(id, playerName, points, airplanesNum, timeInGame));
            }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
        return Players;
    }

    public List<Event> selectTimeTickEvents(int tick_time){
        List<Event> Events;
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT * FROM EVENTS WHERE TICK_TIME = ? ;");
            prepStmt.setInt(1,tick_time);
            ResultSet result  = prepStmt.executeQuery();
            Events = getEventResults(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Events;
    }
    private List<Event> getEventResults(ResultSet result){
        try {
            List<Event> Events = new LinkedList<>();
            int id, tickTime, playerId;
            double xCoordinate, yCoordinate;
            Event.eventType eventType;
            UUID airplaneUUID;
            while (result.next()) {
                id = result.getInt("event_id");
                eventType = Event.eventType.valueOf(result.getString("event_type"));
                tickTime = result.getInt("tick_time");
                playerId = result.getInt("player_id");
                xCoordinate = result.getDouble("x_coor");
                yCoordinate = result.getDouble("y_coor");
                airplaneUUID = UUID.fromString(result.getString("airplane_id"));
                Events.add(new Event(id, eventType, tickTime, playerId, xCoordinate, yCoordinate, airplaneUUID));
            }
            return Events;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFromEvents(int tick_time){
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "DELETE FROM EVENTS WHERE TICK_TIME = ? ;");
            prepStmt.setInt(1,tick_time);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFromPlayers(int player_id){
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "DELETE FROM PLAYERS WHERE PLAYER_ID = ? ;");
            prepStmt.setInt(1,player_id);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            System.err.println("ERROR: Cannot close the connection");
            e.printStackTrace();
        }
    }
}
