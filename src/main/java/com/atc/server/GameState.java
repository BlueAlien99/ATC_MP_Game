package com.atc.server;

import com.atc.client.model.Airplane;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.atc.server.gamelog.GameLog;

import static com.atc.client.Dimensions.CANVAS_HEIGHT;
import static com.atc.client.Dimensions.CANVAS_WIDTH;
import com.atc.client.model.GameSettings;

public class GameState {

    private Thread simulation;

    private ConcurrentHashMap<String, ClientConnection> connections = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Airplane> airplanes = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Airplane> airplanesOutput = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String> chatMessages = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, String> playersLogins = new ConcurrentHashMap<>();
    private GameLog log = new GameLog();
    private int tickCount = 0;

    private final Object outputBufferLock = new Object();

    private GameSettings gs;
    private int gameCount = log.selectGameId();

    public void setGameSettings(GameSettings gs){
        this.gs = gs;
    }

    public void addConnection(String key, ClientConnection value){
        if(connections.isEmpty()){
            simulation = new Thread(new Simulation(this));
            simulation.start();
        }
        if(connections.get(key)!=null){
            //TODO: Warning:(42, 9) 'if' statement has empty body
        }
        else{
            connections.put(key, value);
        }
    }

    public void removeConnection(String key){
        connections.remove(key);
        if(connections.isEmpty()){
            simulation.interrupt(); /*TODO: it won't work, Rafał, as interrupts don't work on threads that have to do with ObjectStreams*/
        }
    }

    //TODO: Find a new way to generate planes!
    public void generateNewAirplanes(int num, UUID owner){
        for(int i = 0; i < num; ++i){
            double pox = CANVAS_WIDTH / 4 + new Random().nextInt((int) CANVAS_WIDTH / 2);
            double poy = CANVAS_HEIGHT / 4 + new Random().nextInt((int) CANVAS_HEIGHT / 2);
            double alt = new Random().nextInt(200)+1500;
            double head = new Random().nextInt(360);
            double speed = new Random().nextInt(100) + 100;
            Airplane airplane = new Airplane(owner, pox, poy, alt, head, speed);

            airplanes.put(airplane.getCallsign(), airplane);
            log.insertCallsign(gameCount,airplane.getUuid(), airplane.getCallsign());
        }
    }

    //TODO: Use recently implemented UUID instead of String ID!
    //TODO: Handle integer overflow in chatMessages and in ClientConnection!
    public void updateAirplane(Airplane airplane, UUID clientUUID){
        if(airplane == null || clientUUID == null){
            return;
        }
        Airplane airplaneInGame = airplanes.get(airplane.getCallsign());
        if(airplaneInGame == null || !clientUUID.equals(airplaneInGame.getOwner())){
            return;
        }
        String chatMsg = ChatMsgParser.parseNewMsg(airplaneInGame, airplane);
        if(chatMsg == null){
            return;
        }
        chatMessages.put(chatMessages.size(), chatMsg);
        airplaneInGame.setTargetParams(airplane.getTargetSpeed(), airplane.getTargetHeading(), airplane.getTargetAltitude());
        sendCommandToDatabase(airplane, clientUUID);
        synchronized (outputBufferLock){
            outputBufferLock.notifyAll();
        }
    }

    private void sendCommandToDatabase(Airplane airplane, UUID clientUUID){
        log.insertEvent(gameCount, "COMMAND", tickCount, clientUUID, playersLogins.get(clientUUID), airplane.getPosX(), airplane.getPosY(),
                airplane.getTargetSpeed(), airplane.getTargetHeading(), airplane.getTargetAltitude(), airplane.getUuid());
//        log.commit();
    }


    public void setNewAirplanesOutput(){
        airplanesOutput = new ConcurrentHashMap<>();
        airplanes.forEach((k, v) -> {
            try {
                airplanesOutput.put(k, (Airplane)v.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        ++tickCount;
        synchronized (outputBufferLock){
            outputBufferLock.notifyAll();
        }
    }

    public Object getOutputBufferLock() {
        return outputBufferLock;
    }

    public ConcurrentHashMap<String, Airplane> getAirplanes() {
        return airplanes;
    }

    public ConcurrentHashMap<String, Airplane> getAirplanesOutput() {
        return airplanesOutput;
    }

    public int getTickCount() {
        return tickCount;
    }

    public ConcurrentHashMap<Integer, String> getChatMessages() {
        return chatMessages;
    }
    public GameLog getLog() {return log;}
    public int getGameCount() {return gameCount;}
    public void addPlayerLogin(UUID playerUUID, String playerLogin){
        playersLogins.put(playerUUID, playerLogin);
    }
    public String searchPlayerLogin(UUID playerUUID){
        return playersLogins.get(playerUUID);
    }
}
