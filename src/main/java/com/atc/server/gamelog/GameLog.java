package com.atc.server.gamelog;

import com.atc.client.model.Checkpoint;
import com.atc.server.model.Event;
import com.atc.server.model.Login;
import com.atc.server.model.Player;


import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;

/**
 * Handle class managing every operation with database
 */
public class GameLog {
    public static final String DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:gamelog.db";

    private HashMap<UUID, Integer> playersUUIDHashmap;

    private Connection con;
    private Statement stat;

    /**
     * Constructor of Gamelog - it needs JDBC driver
     */
    public GameLog(){
        try {
            Class.forName(GameLog.DRIVER);
            connect();
            createTables();
            playersUUIDHashmap = selectPlayerUUIDs();
            closeConnection();
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: missing JDBC driver.");
            e.printStackTrace();
        }
    }

    /**
     * Connects with database
     */
    public void connect(){
        try {
            con = DriverManager.getConnection(DB_URL);
            con.createStatement().execute("PRAGMA foreign_keys = ON");
            stat = con.createStatement();
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

    /**
     * Creates all the tables and triggers
     * @return true if operation was successful, false otherwise
     */
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
        String createTrigger1 ="CREATE TRIGGER IF NOT EXISTS UPDATE_POINTS " +
                "BEFORE INSERT ON EVENTS WHEN NEW.POINTS <> 0 " +
                "BEGIN " +
                "UPDATE PLAYERS " +
                "SET POINTS = NEW.POINTS + (SELECT POINTS FROM PLAYERS WHERE PLAYER_ID = NEW.PLAYER_ID) " +
                "WHERE PLAYER_ID = NEW.PLAYER_ID; " +
                "END; ";
        String createTrigger2 = "CREATE TRIGGER IF NOT EXISTS COMMAND_COOLDOWN_1\n" +
                "BEFORE INSERT ON EVENTS WHEN NEW.EVENT_TYPE == \"COMMAND\" AND (NEW.TICK_TIME - (SELECT TICK_TIME FROM EVENTS WHERE AIRPLANE_UUID == NEW.AIRPLANE_UUID ORDER BY TICK_TIME DESC LIMIT 1) < 5)\n" +
                "BEGIN\n" +
                "UPDATE PLAYERS SET POINTS = (SELECT POINTS FROM PLAYERS WHERE PLAYER_ID = NEW.PLAYER_ID) -5;\n" +
                "END;";
        String createTrigger3 ="CREATE TRIGGER IF NOT EXISTS COMMAND_COOLDOWN_2\n" +
                "BEFORE INSERT ON EVENTS WHEN NEW.EVENT_TYPE == \"COMMAND\" AND (NEW.TICK_TIME - (SELECT TICK_TIME FROM EVENTS WHERE AIRPLANE_UUID == NEW.AIRPLANE_UUID ORDER BY TICK_TIME DESC LIMIT 1) BETWEEN 5 AND 10)\n" +
                "BEGIN\n" +
                "UPDATE PLAYERS SET POINTS = (SELECT POINTS FROM PLAYERS WHERE PLAYER_ID = NEW.PLAYER_ID) -2;\n" +
                "END;";
        String aiPlayer = "insert or ignore into players(player_id, player_uuid) values(-1, null)";
        try {
            connect();
            stat.execute(createPlayers);
            stat.execute(createEvents);
            stat.execute(createLogins);
            stat.execute(createCallsigns);
            stat.execute(createCheckpoints);
            stat.execute(createTrigger1);
            stat.execute(createTrigger2);
            stat.execute(createTrigger3);
            stat.execute(aiPlayer);
            closeConnection();
        } catch (SQLException e) {
            System.err.println("ERROR: Cannot create tables");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Inserts checkpoint to database
     * @param checkpointUUID - UUID
     * @param gameID - ID of game
     * @param points - points
     * @param xPos - x position on canvas
     * @param yPos - y position on canvas
     * @param radius - radius of checkpoint
     */
    public void  insertCheckpoints(UUID checkpointUUID, int gameID, int points,
                                   double xPos, double yPos, double radius){
        try{
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO CHECKPOINTS VALUES(NULL,?,?,?,?,?,?);");
            prepStmt.setBytes(1,getBytesFromUUID(checkpointUUID));
            prepStmt.setInt(2, gameID);
            prepStmt.setInt(3, points);
            prepStmt.setDouble(4, xPos);
            prepStmt.setDouble(5, yPos);
            prepStmt.setDouble(6, radius);
            prepStmt.execute();
            closeConnection();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add checkpoint for:" + checkpointUUID.toString());
            e.printStackTrace();
        }
    }

    /**
     * Inserts login into database
     * @param gameId - ID of game
     * @param playerId - player id in database
     * @param login - player's login
     */
    private void insertLogin(int gameId,int playerId, String login){
        try{
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO LOGINS VALUES(NULL,?,?,?);");
            prepStmt.setInt(1,gameId);
            prepStmt.setInt(2, playerId);
            prepStmt.setString(3,login);
            prepStmt.execute();
            closeConnection();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add login for :" + playerId);
            e.printStackTrace();
        }
    }

    /**
     * Inserts airplane's callsign into database
     * @param game_id - ID of game
     * @param airplaneUUID - UUID of airplane
     * @param callsign - airplane's callsign
     */
    public void  insertCallsign(int game_id, UUID airplaneUUID, String callsign){
        try{
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO CALLSIGNS VALUES(NULL,?,?,?);");
            prepStmt.setInt(1,game_id);
            prepStmt.setBytes(2, getBytesFromUUID(airplaneUUID));
            prepStmt.setString(3,callsign);
            prepStmt.execute();
            closeConnection();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add callsign for:" + airplaneUUID);
            e.printStackTrace();
        }
    }

    /**
     * Inserts player into database
     * @param gameId - ID of game
     * @param playerUUID - UUID of player
     * @param points - points
     * @param airplanesNum - number of airplanes
     * @param timeInGame - time spent in game
     * @return
     */
    public boolean insertPlayer(int gameId, UUID playerUUID, int points, int airplanesNum, double timeInGame){
        try{
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "INSERT INTO PLAYERS VALUES(NULL,?,?,?,?);");
            prepStmt.setBytes(1, getBytesFromUUID(playerUUID));
            prepStmt.setInt(2,points);
            prepStmt.setInt(3, airplanesNum);
            prepStmt.setDouble(4,timeInGame);
            prepStmt.execute();
            closeConnection();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add player:" + playerUUID);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Checks if player exists in database
     * @param playersUUID - UUID os searched player
     * @return true if exists, false otherwise
     */
    private boolean checkUUIDInDatabase(UUID playersUUID){
        return playersUUIDHashmap.containsKey(playersUUID);
    }

    /**
     * Gets player's id in database
     * @param playerUUID - UUID of searched player
     * @return player' ID, 0 otherwise
     */
    public int getPlayerIdFromDatabase(UUID playerUUID){
        if (checkUUIDInDatabase(playerUUID)){
            return playersUUIDHashmap.get(playerUUID);
        }
        return 0;
    }

    /**
     * Inserts Event into database
     * @param gameId - ID of game
     * @param eventType - type of event
     * @param timeTick - tick in which this event occurred
     * @param playerUUID  - UUID of player that caused that event
     * @param points - points
     * @param login - login of player
     * @param xCoordinate - x position on canvas where this event happened
     * @param yCoordinate - y position on canvas where this event happened
     * @param speed - speed in case it was MOVEMENT event
     * @param heading - heading in case it was MOVEMENT event
     * @param height- height in case it was MOVEMENT event
     * @param airplaneUUID - airplane's UUID involved in this event
     * @param airplanesNum - number of airplanes
     * @return
     */
    public boolean insertEvent(int gameId, String eventType, int timeTick, UUID playerUUID, int points,
                               String login, double xCoordinate, double yCoordinate,
                               double speed, double heading, double height,UUID airplaneUUID, int airplanesNum){
        if (playerUUID != null && !checkUUIDInDatabase(playerUUID)) {
            insertPlayer(gameId,playerUUID, 0, airplanesNum, 0);
            int playerIdinDatabese = findPlayerId(playerUUID);
            insertLogin(gameId,playerIdinDatabese, login);
            playersUUIDHashmap.put(playerUUID, playerIdinDatabese);
        }
        int playerId = playerUUID == null ? -1 : playersUUIDHashmap.get(playerUUID);
        //System.out.println(playerId +" " + (playerUUID == null ? "AI" : playerUUID.toString()));
        try{
            connect();
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
            closeConnection();
        }catch (SQLException e){
            System.err.println("ERROR: Cannot add event:" + eventType + ":" + timeTick);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Selects all events from database
     * @return List of events
     */
    public List<Event> selectAllEvents(){
        List<Event> Events;
        try {
            connect();
            ResultSet result = stat.executeQuery("SELECT * FROM EVENTS");
            Events = getEventResults(result);
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Events;
    }

    /**
     * Selects all players from database
     * @return List of players
     */
    public List<Player> selectAllPlayers(){
        List<Player> Players;
        try {
            connect();
            ResultSet result = stat.executeQuery("SELECT * FROM PLAYERS");
            Players = getPlayersResult(result);
            closeConnection();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Players;
    }

    /**
     * Selects player's UUID from database
     * @return Hashmap of UUID's and id's of players in database
     */
    public HashMap<UUID, Integer> selectPlayerUUIDs(){
        HashMap<UUID, Integer> UUIDs = new HashMap<>();
        try {
            connect();
            ResultSet result = stat.executeQuery("SELECT PLAYER_ID, PLAYER_UUID FROM PLAYERS");
            int player_ID;
            UUID playerUUID;
            while(result.next()) {
                player_ID = result.getInt("player_id");
                playerUUID = getUUIDFromBytes(result.getBytes("player_UUID"));
                UUIDs.put(playerUUID,player_ID);
            }
            closeConnection();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return UUIDs;
    }

    /**
     * Helper method to nicely process data from database
     * @param result result set of query in database
     * @return List of players
     */
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

    /**
     * Finds player's ID in database
     * @param playersUUID - UUID of searched player
     * @return id of player in database
     */
    private int findPlayerId(UUID playersUUID){
        int playerId;
        try {
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT PLAYER_ID FROM PLAYERS WHERE PLAYER_UUID = ?;");
            prepStmt.setBytes(1, getBytesFromUUID(playersUUID));
            ResultSet result  = prepStmt.executeQuery();
            playerId = result.getInt(1);
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return playerId;
    }

    /**
     * Selects events from given gameplay
     * @param gameId - ID of game we want to replay
     * @return List of events
     */
    public List<Event> selectGameIdEvents(int gameId){
        List<Event> Events;
        try {
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT * FROM EVENTS WHERE GAME_ID = ?;");
            prepStmt.setInt(1,gameId);
            ResultSet result  = prepStmt.executeQuery();
            Events = getEventResults(result);
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Events;
    }

    /**
     * Selects checkpoints from given gameplay
     * @param gameId - ID of game we want to replay
     * @return List of checkpoints
     */
    public List<Checkpoint> selectCheckpoints(int gameId){
        List<Checkpoint> Checkpoints = new Vector<>();
        UUID checkpointUUID;
        int gameID, points;
        double yPos, xPos, radius;
        try {
            connect();
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
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return Checkpoints;
    }

    /**
     * Selects next gameID from database
     * @return next game ID
     */
    public int selectGameId (){
        int numberOfGames;
        try {
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT COUNT(DISTINCT GAME_ID) FROM EVENTS;");
            ResultSet result  = prepStmt.executeQuery();
            numberOfGames = result.getInt(1);
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return numberOfGames;
    }

    /**
     * Selects game's ID that are present in database
     * @return Vector of gameID's
     */
    public Vector<Integer> selectAvailableGameId (){
        Vector<Integer> availableGames = new Vector<>();
        try {
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "SELECT DISTINCT GAME_ID FROM EVENTS;");
            ResultSet result  = prepStmt.executeQuery();
            while(result.next()){
                availableGames.add(result.getInt(1));
            }
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableGames;
    }

    /**
     * Selects player's logins an ID's from given gameplay
     * @param gameId - game ID
     * @return Hashmap of Logins and Id's from database
     */
    public HashMap<Integer, String> selectPlayerLogin(int gameId){
        HashMap<Integer, String> logins = new HashMap<>();
        try {
            connect();
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
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logins;
    }

    /**
     * Selects all logins from database
     * @return List of logins
     */
    public List<Login> selectAllLogins(){
        List<Login> logins = new Vector<>();
        try {
            connect();
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
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logins;
    }

    /**
     * Selects airplanes' callsigns from database
     * @param gameId - searched game ID
     * @return hashmap of callsigns and corresponding to them UUID's
     */
    public HashMap<UUID, String> selectAirplaneCallsigns(int gameId){
        HashMap<UUID, String> callsigns = new HashMap<>();
        try {
            connect();
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
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return callsigns;
    }

    /**
     * Helper method to nicely process data from database
     * @param result - result set of query in database
     * @return List of events
     */
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

    /**
     * Deletes events from the database from given gameplay
     * @param game_id - ID of game
     */
    public void deleteFromEvents(int game_id){
        try {
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "DELETE FROM EVENTS WHERE GAME_ID = ? ;");
            prepStmt.setInt(1,game_id);
            prepStmt.executeUpdate();
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes player of given ID
     * @param player_id - player's ID
     */
    public void deleteFromPlayers(int player_id){
        try {
            connect();
            PreparedStatement prepStmt = con.prepareStatement(
                    "DELETE FROM PLAYERS WHERE PLAYER_ID = ? ;");
            prepStmt.setInt(1,player_id);
            prepStmt.executeUpdate();
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all data from the database - only for purpose of testing!!!
     */
    public void cleanDatabase(){
        try {
            connect();
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
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Closes connection with database
     */
    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            System.err.println("ERROR: Cannot close the connection");
            e.printStackTrace();
        }
    }


    /**
     * Helper method to transform UUID into table of bytes
     * @author jeffjohnson9046
     * @link https://gist.github.com/jeffjohnson9046/c663dd22bbe6bb0b3f5e
     * @param uuid
     * @return
     */

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }
    /**
     * Helper method to transform table of bytes into UUID
     * @author jeffjohnson9046
     * @link https://gist.github.com/jeffjohnson9046/c663dd22bbe6bb0b3f5e
     * @param bytes
     * @return
     */
    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
}
