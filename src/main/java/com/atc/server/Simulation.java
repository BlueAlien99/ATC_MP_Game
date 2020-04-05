package com.atc.server;

public class Simulation implements Runnable{

    private GameState gameState;

    public Simulation(GameState gameState) {
        this.gameState = gameState;
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
            System.out.println("1s has passed");
        }
    }
}
