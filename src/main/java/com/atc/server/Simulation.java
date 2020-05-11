package com.atc.server;

import com.atc.client.model.Airplane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.Dimensions.CANVAS_HEIGHT;
import static com.atc.client.Dimensions.CANVAS_WIDTH;

public class Simulation implements Runnable{

    private GameState gameState;
    private ConcurrentHashMap<String, Airplane> airplanes;

    private Timer t;
    public ActionListener timerListener;

    private class TimeMover implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            airplanes.forEach((k, v) -> {
                v.moveAirplane();
                //This is just for testing, so that user does not loose airplanes
                if(v.getPositionX()<0) v.setCurrPosX(CANVAS_WIDTH);
                if(v.getPositionY()<0) v.setCurrPosY(CANVAS_HEIGHT);
                if(v.getPositionX()>CANVAS_WIDTH) v.setCurrPosX(0);
                if(v.getPositionY()>CANVAS_HEIGHT) v.setCurrPosY(0);
                System.out.println(v.toString());
            });
            gameState.setNewAirplanesOutput();
            System.out.println("1s has passed");
        }
    }

    public Simulation(GameState gameState) {
        this.gameState = gameState;
        this.airplanes = gameState.getAirplanes();
    }

    @Override
    public void run() {
        System.out.println("1s has passed");

        timerListener = new TimeMover();
        t = new Timer(1000, timerListener);

        t.start();

        /*
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
         */
    }
}
