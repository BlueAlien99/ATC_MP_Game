package com.atc.server;

import com.atc.client.model.Airplane;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.Dimensions.CANVAS_HEIGHT;
import static com.atc.client.Dimensions.CANVAS_WIDTH;

public class GameState {

    private Thread simulation;

    private ConcurrentHashMap<String, ClientConnection> connections = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Airplane> airplanes = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Airplane> airplanesOutput = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String> chatMessages = new ConcurrentHashMap<>();

    private int tickCount = 0;

    private final Object outputBufferLock = new Object();

    public void addConnection(String key, ClientConnection value){
        if(connections.isEmpty()){
            simulation = new Thread(new Simulation(this));
            simulation.start();
        }
        if(connections.get(key)!=null){

        }
        else{
            connections.put(key, value);
        }
    }

    public void removeConnection(String key){
        connections.remove(key);
        if(connections.isEmpty()){
            simulation.interrupt(); /*it won't work, Rafał, as interrupts don't work on threads that have to do with ObjectStreams*/
        }
    }

    //TODO: Find a new way to generate planes!
    public void generateNewAirplanes(int num, UUID owner){
        for(int i = 0; i < num; ++i){
            Airplane airplane = new Airplane(owner, 100);
            airplane.setMaxSpeed(1000);
            airplane.setMinSpeed(0);
            airplane.setCurrHeading(new Random().nextInt(360));
            airplane.setTargetHeading(airplane.getCurrHeading());
            airplane.setCurrHeight(new Random().nextInt(200)+200);
            airplane.setTargetHeight(airplane.getCurrHeight()+new Random().nextInt(400)-200);
            airplane.setCurrPosX(CANVAS_WIDTH/4+new Random().nextInt((int)CANVAS_WIDTH/2));
            airplane.setCurrPosY(CANVAS_HEIGHT/4+new Random().nextInt((int)CANVAS_HEIGHT/2));
            airplane.setCurrSpeed(200);
            airplane.setTargetSpeed(airplane.getCurrSpeed()+new Random().nextInt(100)-50);

            airplanes.put(airplane.getId(), airplane);
        }
    }

    //TODO: Use recently implemented UUID instead of String ID!
    //TODO: Handle integer overflow in chatMessages and in ClientConnection!
    public void updateAirplane(Airplane airplane, UUID clientUUID){
        if(airplane == null || clientUUID == null){
            return;
        }
        Airplane airplaneInGame = airplanes.get(airplane.getId());
        if(airplaneInGame == null || !clientUUID.equals(airplaneInGame.getOwner())){
            return;
        }
        String chatMsg = ChatMsgParser.parseNewMsg(airplaneInGame, airplane);
        chatMessages.put(chatMessages.size(), chatMsg);
        airplaneInGame.setNewTargets(airplane.getTargetSpeed(), airplane.getTargetHeading(), airplane.getTargetHeight());
        synchronized (outputBufferLock){
            outputBufferLock.notifyAll();
        }
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
}
