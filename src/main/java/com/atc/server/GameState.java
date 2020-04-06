package com.atc.server;

import com.atc.client.model.Airplane;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private Thread simulation;

    private ConcurrentHashMap<String, ClientConnection> connections = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Airplane> airplanes = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Airplane> airplanesOutput = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String> chatMessages = new ConcurrentHashMap<>();

    private final Object outputBufferLock = new Object();

    public void addConnection(String key, ClientConnection value){
        if(connections.isEmpty()){
            simulation = new Thread(new Simulation(this));
            simulation.start();
        }
        connections.put(key, value);
    }

    public void removeConnection(String key){
        connections.remove(key);
        if(connections.isEmpty()){
            simulation.interrupt();
        }
    }

    //TODO: Find a new way to generate planes!
    public void generateNewAirplanes(int num, Socket socket){
        String owner = socket.toString();
        for(int i = 0; i < num; ++i){
            Airplane airplane = new Airplane(owner, 112.5 + i*22.5);
            airplanes.put(airplane.getId(), airplane);
        }
    }

    public void updateAirplane(Airplane airplane, Socket socket){
        Airplane airplaneInGame = airplanes.get(airplane.getId());
        if(airplaneInGame == null || !socket.toString().equals(airplaneInGame.getOwner())){
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
}
