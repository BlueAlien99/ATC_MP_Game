package com.atc.server;

import com.atc.client.model.Airplane;

import java.util.concurrent.ConcurrentHashMap;

public class Simulation implements Runnable{

    private GameState gameState;
    private ConcurrentHashMap<String, Airplane> airplanes;

    public Simulation(GameState gameState) {
        this.gameState = gameState;
        this.airplanes = gameState.getAirplanes();
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e){
                e.printStackTrace();
                return;
            }
            airplanes.forEach((k, v) -> {
                v.moveAirplane();
                System.out.println(v.toString());
            });
            //TODO: Test deep copy!
            gameState.setNewAirplanesOutput();
            System.out.println("1s has passed");
        }
    }
}
