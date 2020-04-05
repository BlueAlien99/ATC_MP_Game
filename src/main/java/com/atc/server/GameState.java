package com.atc.server;

import com.atc.client.model.Airplane;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private Thread simulation;

    private ConcurrentHashMap<String, ClientConnection> connections = new ConcurrentHashMap<>();

    private ArrayList<Airplane> airplanes;

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
}
