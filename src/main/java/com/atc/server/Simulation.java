package com.atc.server;

import com.atc.client.model.Airplane;
import javafx.util.Pair;

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

            //TCAS
            //TODO: it doesnt take altitude into calculations, but its easy peasy to implement
            //TODO: too sensitive to speed
            //TODO: add collision detection, just calculate distance between two aircrafts
            //TODO: add detection, when two aircrafts are just too close to each other
            ArrayList<Airplane> airList = new ArrayList<>(airplanes.values());
            double tooClose = 256;
            double criticallyClose = 64;
            for(int i = 0; i < airList.size(); ++i){
                Airplane iel = airList.get(i);
                char ielCase = getTcasCase(iel.getCurrHeading());
                for(int j = i+1; j < airList.size(); ++j){
                    Airplane jel = airList.get(j);
                    char jelCase = getTcasCase(jel.getCurrHeading());
                    boolean collision = false;

                    if((ielCase == '8' || ielCase == '2') && (jelCase == '8' || jelCase == '2') && Math.abs(iel.getPositionX() - jel.getPositionX()) < tooClose){
                        if(ielCase != jelCase){
                            double coordFar = ielCase == '8' ? iel.getPositionY() : jel.getPositionY();
                            double coordClose = ielCase == '8' ? jel.getPositionY() : iel.getPositionY();
                            if(coordFar - coordClose > 0 && Math.abs(coordFar - coordClose) < iel.getCurrSpeed() + jel.getCurrSpeed()){
                                collision = true;
                            }
                        }
                    }

                    else if((ielCase == '6' || ielCase == '4') && (jelCase == '6' || jelCase == '4') && Math.abs(iel.getPositionY() - jel.getPositionY()) < tooClose){
                        if(ielCase != jelCase){
                            double coordFar = ielCase == '6' ? iel.getPositionX() : jel.getPositionX();
                            double coordClose = ielCase == '6' ? jel.getPositionX() : iel.getPositionX();
                            if(coordFar - coordClose < 0 && Math.abs(coordFar - coordClose) < iel.getCurrSpeed() + jel.getCurrSpeed()){
                                collision = true;
                            }
                        }
                    }

                    else {
                        if (jelCase == '8' || jelCase == '2') {
                            char temp = jelCase;
                            jelCase = ielCase;
                            ielCase = temp;
                            Airplane templane = jel;
                            jel = iel;
                            iel = templane;
                        }

                        double colx, coly;

                        if((ielCase == '8' || ielCase == '2') && (jelCase == '4' || jelCase == '6')){
                            colx = iel.getPositionX();
                            coly = jel.getPositionY();
                        }
                        else if(ielCase == '8' || ielCase == '2'){
                            colx = iel.getPositionX();
                            coly = jel.getColAParam() * colx + jel.getColBParam();
                        }
                        else{
                            colx = (jel.getColBParam() - iel.getColBParam()) / (iel.getColAParam() - jel.getColAParam());
                            coly = iel.getColAParam() * colx + iel.getColBParam();
                        }

                        double disi = Math.sqrt(Math.pow(colx - iel.getPositionX(), 2) + Math.pow(coly - iel.getPositionY(), 2));
                        double disj = Math.sqrt(Math.pow(colx - jel.getPositionX(), 2) + Math.pow(coly - jel.getPositionY(), 2));

                        // check if collision point is in front of a plane
                        boolean ieldir = (ielCase == '8' && coly < iel.getPositionY()) || (ielCase == '2' && coly > iel.getPositionY()) || ((ielCase == '9' || ielCase == '6' || ielCase == '3') && colx > iel.getPositionX()) || ((ielCase == '1' || ielCase == '4' || ielCase == '7') && colx < iel.getPositionX());
                        boolean jeldir = (jelCase == '8' && coly < jel.getPositionY()) || (jelCase == '2' && coly > jel.getPositionY()) || ((jelCase == '9' || jelCase == '6' || jelCase == '3') && colx > jel.getPositionX()) || ((jelCase == '1' || jelCase == '4' || jelCase == '7') && colx < jel.getPositionX());

                        if(ieldir && jeldir && disi < iel.getCurrSpeed() && disj < jel.getCurrSpeed()){
                            collision = true;
                        }
                    }

                    if(collision){
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

    //TODO: all tcas methods should probably be moved to a separate static class
    private char getTcasCase(double heading){
        double delta = 2.5;
        if(heading < delta || heading > 360-delta){
            return '8';
        } else if(heading >= delta && heading <= 90-delta){
            return '9';
        } else if(heading > 90-delta && heading < 90+delta){
            return '6';
        } else if(heading >= 90+delta && heading <= 180-delta){
            return '3';
        } else if(heading > 180-delta && heading < 180+delta){
            return '2';
        } else if(heading >= 180+delta && heading <= 270-delta){
            return '1';
        } else if(heading > 270-delta && heading < 270+delta){
            return '4';
        } else if(heading >= 270+delta && heading <= 360-delta){
            return '7';
        }
        return 0;
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
