package com.atc.server.gamelog;

import com.atc.server.model.Event;
import com.atc.server.model.Player;


import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class GameLog {
    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:gamelog.db";

    private HashMap<UUID, Integer> playersUUIDHashmap;

    private Connection con;
    private Statement stat;

    public GameLog(){
        try {
            Class.forName(GameLog.DRIVER);
            connect();
            createTables();
            playersUUIDHashmap = selectPlayerUUIDs();
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: missing JDBC driver.");
            e.printStackTrace();
        }
    }

    public void connect(){
        try {
            con = DriverManager.getConnection(DB_URL);
            con.createStatement().execute("PRAGMA foreign_keys = ON");
            stat = con.createStatement();
            createTables();
        } catch (SQLException e) {
            System.err.println("ERROR: cannot open connection.");
            e.printStackTrace();
        }
    }

    public void commit(){
        try {
            con.commit();
        }catch (SQLException e){
            System.err.println("ERROR: cannot commit.");
            e.printStackTrace();
        }
    }

    private boolean createTables(){
        String createPlayers = "CREATE TABLE IF NOT EXISTS PLAYERS" +
                "(PLAYER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PLAYER_UUID BLOB NOT NULL," +
                "POINTS INTEGER NOT NULL DEFAULT 0," +
                "AIRPLANES_NUM INTEGER NOT NULL DEFAULT 0," +
                "TIME_IN_GAME DOUBLE DEFAULT 0)";
        String createEvents = "CREATE TABLE IF NOT EXISTS EVENTS" +
                "(EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "GAME_ID INTEGER NOT NULL," +
                "EVENT_TYPE VARCHAR(255) NOT NULL," +
                "TICK_TIME INTEGER NOT NULL," +
                "PLAYER_ID INTEGER NOT NULL," +
                "X_COOR DOUBLE," +
                "Y_COOR DOUBLE," +
                "SPEED DOUBLE," +
                "HEADING DOUBLE," +
                "HEIGHT DOUBLE," +
                "AIRPLANE_UUID BLOB NOT NULL," +
                "FOREIGN KEY(PLAYER_ID) REFERENCES PLAYERS(PLAYER_ID))";
        String createLogins ="CREATE TABLE IF NOT EXISTS LOGINS" +
                "(LOGIN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "GAME_ID INTEGER NOT NULL," +
                "PLAYER_ID INTEGER NOT NULL," +
                "PLAYER_LOGIN VARCHAR(255) NOT NULL)";

        String createCallsigns = "CREATE TABLE IF NOT EXISTS CALLSIGNS" +
                "(CALLSIGN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "GAME_ID INTEGER NOT NULL," +
                "AIRPLANE_UUID BLOB NOT NULL," +
                "AIRPLANE_CALLSIGN VARCHAR(255) NOT NULL)";

        try {
            stat.execute(createPlayers);
            stat.execute(createEvents);
            stat.execute(createLogins);
            stat.execute(createCallsigns);
        } catch (SQLException e) {
            System.err.println("ERROR: Cannot create tables");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void insertLogin(int gameId,int playerId, String login){
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO LOGINS VALUES(NULL,?,?,?);");
            prepStmt.setInt(1,gameId);
            prepStmt.setInt(2, playerId);
            prepStmt.setString(3,login);
            prepStmt.execute();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add login for :" + playerId);
            e.printStackTrace();
        }
    }

    public void  insertCallsign(int game_id, UUID airplaneUUID, String callsign){
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO CALLSIGNS VALUES(NULL,?,?,?);");
            prepStmt.setInt(1,game_id);
            prepStmt.setBytes(2, airplaneUUID.toString().getBytes());
            prepStmt.setString(3,callsign);
            prepStmt.execute();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add callsign for:" + airplaneUUID);
            e.printStackTrace();
        }
    }

    private boolean insertPlayer(int gameId, UUID playerUUID, int points, int airplanesNum, double timeInGame){
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO PLAYERS VALUES(NULL,?,?,?,?);");
            prepStmt.setBytes(1, playerUUID.toString().getBytes());
            prepStmt.setInt(2,points);
            prepStmt.setInt(3, airplanesNum);
            prepStmt.setDouble(4,timeInGame);
            prepStmt.execute();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add player:" + playerUUID);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean checkUUIDInDatabase(UUID playersUUID){
        return playersUUIDHashmap.containsKey(playersUUID);
    }
    public int getPlayerIdFromDatabase(UUID playerUUID){
        if (checkUUIDInDatabase(playerUUID)){
            return playersUUIDHashmap.get(playerUUID);
        }
        return 0;
    }
    private void printHashmap(){
        playersUUIDHashmap.entrySet().forEach(entry ->
            System.out.println(entry.getKey() + " " + entry.getValue()));
    }

    public boolean insertEvent(int gameId, String eventType, int timeTick, UUID playerUUID, String login, double xCoordinate, double yCoordinate,
                               double speed, double heading, double height,UUID airplaneUUID){
        if (!checkUUIDInDatabase(playerUUID)) {
            insertPlayer(gameId,playerUUID, 0, 0, 0);
            int playerIdinDatabese = findPlayerId(playerUUID);
            insertLogin(gameId,playerIdinDatabese, login);
//            commit();
            playersUUIDHashmap.put(playerUUID, playerIdinDatabese);
        }
        int playerId = playersUUIDHashmap.get(playerUUID);
        System.out.println(playerId +" " + playerUUID.toString());
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO EVENTS VALUES(NULL,?,?,?,?,?,?,?,?,?,?);");
            prepStmt.setInt(1,gameId);
            prepStmt.setString(2,eventType);
            prepStmt.setInt(3, timeTick);
            prepStmt.setInt(4, playerId);
            prepStmt.setDouble(5, xCoordinate);
            prepStmt.setDouble(6, yCoordinate);
            prepStmt.setDouble(7, speed);
            prepStmt.setDouble(8, heading);
            prepStmt.setDouble(9, height);
            prepStmt.setBytes(10, airplaneUUID.toString().getBytes());
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

    public HashMap<UUID, Integer> selectPlayerUUIDs(){
        HashMap<UUID, Integer> UUIDs = new HashMap<>();
        try {
            ResultSet result = stat.executeQuery("SELECT PLAYER_ID, PLAYER_UUID FROM PLAYERS");
            int player_ID;
            UUID playerUUID;
            while(result.next()) {
                player_ID = result.getInt("player_id");
                playerUUID = UUID.nameUUIDFromBytes(result.getBytes("player_UUID"));
                UUIDs.put(playerUUID,player_ID);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return UUIDs;
    }

    private List<Player> getPlayersResult(ResultSet result){
        List<Player> Players = new LinkedList<>();
        try{
            int id, points, airplanesNum;
            double timeInGame;
            UUID playerUUID;
            while(result.next()) {
                id = result.getInt("player_id");
                playerUUID = UUID.nameUUIDFromBytes(result.getBytes("player_UUID"));
                points = result.getInt("points");
                airplanesNum = result.getInt("airplanes_num");
                timeInGame = result.getDouble("time_in_game");
                Players.add(new Player(id, playerUUID, points, airplanesNum, timeInGame));
            }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
        return Players;
    }
    private int findPlayerId(UUID playersUUID){
        int playerId;
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT PLAYER_ID FROM PLAYERS WHERE PLAYER_UUID = ?;");
            prepStmt.setBytes(1,playersUUID.toString().getBytes());
            ResultSet result  = prepStmt.executeQuery();
            playerId = result.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return playerId;
    }
    public List<Event> selectGameIdEvents(int gameId){
        List<Event> Events;
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT * FROM EVENTS WHERE GAME_ID = ?;");
            prepStmt.setInt(1,gameId);
            ResultSet result  = prepStmt.executeQuery();
            Events = getEventResults(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Events;
    }

    public int selectGameId (){
        int numberOfGames;
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT COUNT(DISTINCT GAME_ID) FROM EVENTS;");
            ResultSet result  = prepStmt.executeQuery();
            numberOfGames = result.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return numberOfGames;
    }

    public HashMap<Integer, String> selectPlayerLogin(int gameId){
        HashMap<Integer, String> logins = new HashMap<>();
        try {
            int playerId;
            String login;
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT PLAYER_ID, PLAYER_LOGIN FROM LOGINS WHERE GAME_ID = ?;");
            prepStmt.setInt(1,gameId);
            ResultSet result  = prepStmt.executeQuery();
            while(result.next()){
                playerId = result.getInt(1);
                login = result.getString(2);
                logins.put(playerId,login);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logins;
    }

    public HashMap<UUID, String> selectAirplaneCallsigns(int gameId){
        HashMap<UUID, String> callsigns = new HashMap<>();
        try {
            UUID airplaneUUID;
            String callsign;
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT AIRPLANE_UUID, AIRPLANE_CALLSIGN FROM CALLSIGNS WHERE GAME_ID = ?;");
            prepStmt.setInt(1,gameId);
            ResultSet result  = prepStmt.executeQuery();
            while(result.next()){
                airplaneUUID = UUID.nameUUIDFromBytes(result.getBytes(1));
                callsign = result.getString(2);
                callsigns.put(airplaneUUID,callsign);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return callsigns;
    }


    private List<Event> getEventResults(ResultSet result){
        try {
            List<Event> Events = new LinkedList<>();
            int id, gameId, tickTime, playerId;
            double xCoordinate, yCoordinate, speed, heading, height;
            Event.eventType eventType;
            UUID airplaneUUID;
            while (result.next()) {
                id = result.getInt("event_id");
                gameId = result.getInt("game_id");
                eventType = Event.eventType.valueOf(result.getString("event_type"));
                tickTime = result.getInt("tick_time");
                playerId = result.getInt("player_id");
                xCoordinate = result.getDouble("x_coor");
                yCoordinate = result.getDouble("y_coor");
                speed = result.getDouble("speed");
                heading = result.getDouble("heading");
                height = result.getDouble("height");
                airplaneUUID = UUID.nameUUIDFromBytes(result.getBytes("airplane_UUID"));
                Events.add(new Event(id, gameId, eventType, tickTime, playerId, xCoordinate, yCoordinate,speed,
                        heading, height,airplaneUUID));
            }
            return Events;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFromEvents(int game_id, int timeTick){
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "DELETE FROM EVENTS WHERE GAME_ID = ? ;");
            prepStmt.setInt(1,game_id);
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
