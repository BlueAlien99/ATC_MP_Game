package com.atc.server;

import com.atc.client.model.Airplane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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
                //used for collision detection, remove when 4 ifs above are removed
                v.calculateABParams();

                gameState.getLog().insertEvent(
                        gameState.getGameCount(), "MOVEMENT", gameState.getTickCount(), v.getOwner(),
                        gameState.searchPlayerLogin(v.getOwner()),
                        v.getPositionX(), v.getPositionY(), v.getCurrSpeed(), v.getCurrHeading(),
                        v.getCurrHeight(), v.getUid());
                System.out.println(v.toString());
            });

            ArrayList<Airplane> airList = new ArrayList<>(airplanes.values());
            for(int i = 0; i < airList.size(); ++i){
                for(int j = i+1; j < airList.size(); ++j){
                    Airplane iel = airList.get(i);
                    Airplane jel = airList.get(j);

                    double x = (jel.getColBParam() - iel.getColBParam()) / (iel.getColAParam() - jel.getColAParam());
                    double y = iel.getColAParam() * x + iel.getColBParam();

                    double disi = Math.sqrt(Math.pow(x - iel.getPositionX(), 2) + Math.pow(y - iel.getPositionY(), 2));
                    double disj = Math.sqrt(Math.pow(x - jel.getPositionX(), 2) + Math.pow(y - jel.getPositionY(), 2));

                    if(disi < iel.getCurrSpeed() && disj < jel.getCurrSpeed()){
                        iel.setCollisionCourse();
                        jel.setCollisionCourse();
                    }
                }
            }

//            gameState.getLog().commit();
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
