package com.atc.server;

import com.atc.client.model.Airplane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
                try {
                    if (v.getPosX() < 0) v.setPosX(CANVAS_WIDTH);
                    if (v.getPosY() < 0) v.setPosY(CANVAS_HEIGHT);
                    if (v.getPosX() > CANVAS_WIDTH) v.setPosX(0);
                    if (v.getPosY() > CANVAS_HEIGHT) v.setPosY(0);
                    //used for collision detection, remove when 4 ifs above are removed
                    v.calculateABParams();
                } catch (Exception ex){
                    //TODO: todo
                }


                gameState.getLog().insertEvent(
                        gameState.getGameCount(), "MOVEMENT", gameState.getTickCount(), v.getOwner(),
                        gameState.searchPlayerLogin(v.getOwner()),
                        v.getPosX(), v.getPosY(), v.getSpeed(), v.getHeading(),
                        v.getAltitude(), v.getUuid());
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
                char ielCase = getTcasCase(iel.getHeading());
                for(int j = i+1; j < airList.size(); ++j){
                    Airplane jel = airList.get(j);
                    char jelCase = getTcasCase(jel.getHeading());
                    boolean collision = false;

                    if((ielCase == '8' || ielCase == '2') && (jelCase == '8' || jelCase == '2') && Math.abs(iel.getPosX() - jel.getPosX()) < tooClose){
                        if(ielCase != jelCase){
                            double coordFar = ielCase == '8' ? iel.getPosY() : jel.getPosY();
                            double coordClose = ielCase == '8' ? jel.getPosY() : iel.getPosY();
                            if(coordFar - coordClose > 0 && Math.abs(coordFar - coordClose) < iel.getSpeed() + jel.getSpeed()){
                                collision = true;
                            }
                        }
                    }

                    else if((ielCase == '6' || ielCase == '4') && (jelCase == '6' || jelCase == '4') && Math.abs(iel.getPosY() - jel.getPosY()) < tooClose){
                        if(ielCase != jelCase){
                            double coordFar = ielCase == '6' ? iel.getPosX() : jel.getPosX();
                            double coordClose = ielCase == '6' ? jel.getPosX() : iel.getPosX();
                            if(coordFar - coordClose < 0 && Math.abs(coordFar - coordClose) < iel.getSpeed() + jel.getSpeed()){
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
                            colx = iel.getPosX();
                            coly = jel.getPosY();
                        }
                        else if(ielCase == '8' || ielCase == '2'){
                            colx = iel.getPosX();
                            coly = jel.getColAParam() * colx + jel.getColBParam();
                        }
                        else{
                            colx = (jel.getColBParam() - iel.getColBParam()) / (iel.getColAParam() - jel.getColAParam());
                            coly = iel.getColAParam() * colx + iel.getColBParam();
                        }

                        double disi = Math.sqrt(Math.pow(colx - iel.getPosX(), 2) + Math.pow(coly - iel.getPosY(), 2));
                        double disj = Math.sqrt(Math.pow(colx - jel.getPosX(), 2) + Math.pow(coly - jel.getPosY(), 2));

                        // check if collision point is in front of a plane
                        boolean ieldir = (ielCase == '8' && coly < iel.getPosY()) || (ielCase == '2' && coly > iel.getPosY()) || ((ielCase == '9' || ielCase == '6' || ielCase == '3') && colx > iel.getPosX()) || ((ielCase == '1' || ielCase == '4' || ielCase == '7') && colx < iel.getPosX());
                        boolean jeldir = (jelCase == '8' && coly < jel.getPosY()) || (jelCase == '2' && coly > jel.getPosY()) || ((jelCase == '9' || jelCase == '6' || jelCase == '3') && colx > jel.getPosX()) || ((jelCase == '1' || jelCase == '4' || jelCase == '7') && colx < jel.getPosX());

                        if(ieldir && jeldir && disi < iel.getSpeed() && disj < jel.getSpeed()){
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
