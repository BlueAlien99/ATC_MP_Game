package com.atc.server.gamelog;

import com.atc.client.model.Checkpoint;
import com.atc.server.model.Event;
import com.atc.server.model.Login;
import com.atc.server.model.Player;


import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;

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
                "POINTS INTEGER NOT NULL," +
                "X_COOR DOUBLE," +
                "Y_COOR DOUBLE," +
                "SPEED DOUBLE," +
                "HEADING DOUBLE," +
                "HEIGHT DOUBLE," +
                "AIRPLANE_UUID BLOB NOT NULL)";
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
        String createCheckpoints = "CREATE TABLE IF NOT EXISTS CHECKPOINTS" +
                "(CHECKPOINT_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "CHECKPOINT_UUID BLOB NOT NULL," +
                "GAME_ID INTEGER NOT NULL," +
                "POINTS INTEGER NOT NULL," +
                "X_POS DOUBLE NOT NULL," +
                "Y_POS DOUBLE NOT NULL," +
                "RADIUS DOUBLE NOT NULL)";
        String createTrigger ="CREATE TRIGGER IF NOT EXISTS UPDATE_POINTS " +
                "BEFORE INSERT ON EVENTS WHEN NEW.POINTS <> 0 " +
                "BEGIN " +
                "UPDATE PLAYERS " +
                "SET POINTS = NEW.POINTS + (SELECT POINTS FROM PLAYERS WHERE PLAYER_ID = NEW.PLAYER_ID) " +
                "WHERE PLAYER_ID = NEW.PLAYER_ID; " +
                "END; ";
        try {
            stat.execute(createPlayers);
            stat.execute(createEvents);
            stat.execute(createLogins);
            stat.execute(createCallsigns);
            stat.execute(createCheckpoints);
            stat.execute(createTrigger);
        } catch (SQLException e) {
            System.err.println("ERROR: Cannot create tables");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void  insertCheckpoints(UUID checkpointUUID, int gameID, int points,
                                   double xPos, double yPos, double radius){
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO CHECKPOINTS VALUES(NULL,?,?,?,?,?,?);");
            prepStmt.setBytes(1,getBytesFromUUID(checkpointUUID));
            prepStmt.setInt(2, gameID);
            prepStmt.setInt(3, points);
            prepStmt.setDouble(4, xPos);
            prepStmt.setDouble(5, yPos);
            prepStmt.setDouble(6, radius);
            prepStmt.execute();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add checkpoint for:" + checkpointUUID.toString());
            e.printStackTrace();
        }
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
            prepStmt.setBytes(2, getBytesFromUUID(airplaneUUID));
            prepStmt.setString(3,callsign);
            prepStmt.execute();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add callsign for:" + airplaneUUID);
            e.printStackTrace();
        }
    }

    public boolean insertPlayer(int gameId, UUID playerUUID, int points, int airplanesNum, double timeInGame){
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO PLAYERS VALUES(NULL,?,?,?,?);");
            prepStmt.setBytes(1, getBytesFromUUID(playerUUID));
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

    public boolean insertEvent(int gameId, String eventType, int timeTick, UUID playerUUID, int points,
                               String login, double xCoordinate, double yCoordinate,
                               double speed, double heading, double height,UUID airplaneUUID, int airplanesNum){
        if (!checkUUIDInDatabase(playerUUID)) {
            insertPlayer(gameId,playerUUID, 0, airplanesNum, 0);
            int playerIdinDatabese = findPlayerId(playerUUID);
            insertLogin(gameId,playerIdinDatabese, login);
//            commit();
            playersUUIDHashmap.put(playerUUID, playerIdinDatabese);
        }
        int playerId = playersUUIDHashmap.get(playerUUID);
        System.out.println(playerId +" " + playerUUID.toString());
        try{
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO EVENTS VALUES(NULL,?,?,?,?,?,?,?,?,?,?,?);");
            prepStmt.setInt(1,gameId);
            prepStmt.setString(2,eventType);
            prepStmt.setInt(3, timeTick);
            prepStmt.setInt(4, playerId);
            prepStmt.setInt(5, points);
            prepStmt.setDouble(6, xCoordinate);
            prepStmt.setDouble(7, yCoordinate);
            prepStmt.setDouble(8, speed);
            prepStmt.setDouble(9, heading);
            prepStmt.setDouble(10, height);
            prepStmt.setBytes(11, getBytesFromUUID(airplaneUUID));
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
                playerUUID = getUUIDFromBytes(result.getBytes("player_UUID"));
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
                playerUUID = getUUIDFromBytes(result.getBytes("player_UUID"));
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
            prepStmt.setBytes(1, getBytesFromUUID(playersUUID));
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

    public List<Checkpoint> selectCheckpoints(int gameId){
        List<Checkpoint> Checkpoints = new Vector<>();
        UUID checkpointUUID;
        int gameID, points;
        double yPos, xPos, radius;
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT * FROM CHECKPOINTS WHERE GAME_ID = ?;");
            prepStmt.setInt(1,gameId);
            ResultSet result  = prepStmt.executeQuery();
            while(result.next()){
                checkpointUUID = getUUIDFromBytes(result.getBytes("checkpoint_uuid"));
                gameID = result.getInt("game_id");
                points = result.getInt("points");
                yPos = result.getDouble("y_pos");
                xPos = result.getDouble("x_pos");
                radius = result.getDouble("radius");
                Checkpoints.add(new Checkpoint(checkpointUUID,gameID,points,
                        xPos, yPos, radius));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Checkpoints;
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
    public Vector<Integer> selectAvailableGameId (){
        Vector<Integer> availableGames = new Vector<>();
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT DISTINCT GAME_ID FROM EVENTS;");
            ResultSet result  = prepStmt.executeQuery();
            while(result.next()){
                availableGames.add(result.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableGames;
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

    public List<Login> selectAllLogins(){
        List<Login> logins = new Vector<>();
        try {
            int playerId, gameID;
            String login;
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT PLAYER_ID,GAME_ID,  PLAYER_LOGIN FROM LOGINS;");
            ResultSet result  = prepStmt.executeQuery();
            while(result.next()){
                playerId = result.getInt("player_id");
                login = result.getString("player_login");
                gameID = result.getInt("game_id");
                logins.add(new Login(gameID, playerId, login));
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
                airplaneUUID = getUUIDFromBytes(result.getBytes(1));
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
            int id, gameId, tickTime, playerId, points;
            double xCoordinate, yCoordinate, speed, heading, height;
            Event.eventType eventType;
            UUID airplaneUUID;
            while (result.next()) {
                id = result.getInt("event_id");
                gameId = result.getInt("game_id");
                eventType = Event.eventType.valueOf(result.getString("event_type"));
                tickTime = result.getInt("tick_time");
                playerId = result.getInt("player_id");
                points = result.getInt("points");
                xCoordinate = result.getDouble("x_coor");
                yCoordinate = result.getDouble("y_coor");
                speed = result.getDouble("speed");
                heading = result.getDouble("heading");
                height = result.getDouble("height");
                airplaneUUID = getUUIDFromBytes(result.getBytes("airplane_UUID"));
                Events.add(new Event(id, gameId, eventType, tickTime, playerId,points, xCoordinate, yCoordinate,speed,
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

    public void cleanDatabase(){
        try {
            PreparedStatement prepStmt = con.prepareStatement(
                    "DELETE FROM PLAYERS ;");
            prepStmt.executeUpdate();
            prepStmt = con.prepareStatement("DELETE FROM CHECKPOINTS ;");
            prepStmt.executeUpdate();
            prepStmt = con.prepareStatement("DELETE FROM EVENTS ;");
            prepStmt.executeUpdate();
            prepStmt = con.prepareStatement("DELETE FROM CALLSIGNS ;");
            prepStmt.executeUpdate();
            prepStmt = con.prepareStatement("DELETE FROM LOGINS ;");
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

    /////////////////////////////////////////////////////////////////////////////////////
    /// THIS IS WORK OF jeffjohnson9046
    /// LINK TO REPOSITORY https://gist.github.com/jeffjohnson9046/c663dd22bbe6bb0b3f5e
    /////////////////////////////////////////////////////////////////////////////////////

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
}
