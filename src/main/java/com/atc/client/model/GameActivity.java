package com.atc.client.model;

import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.Dimensions.*;
import static java.lang.System.currentTimeMillis;

public class GameActivity {

    private long gameStartedTime;
    private Map<UUID, ArrayList<TrailDot>> gameHistory;
    private Map<UUID, Airplane> gameAirplanes;

    public StackPane radar;

    public GameCanvas gameCanvas;


    static class TrailDot{
        public double xPos;
        public double yPos;
        public double heading;
        public double altitude;
        public double speed;

        public long creationTime;

        TrailDot(Airplane airplane){
            xPos = airplane.getPositionX();
            yPos = airplane.getPositionY();
            heading = airplane.getCurrHeading();
            altitude = airplane.getCurrHeight();
            speed = airplane.getCurrSpeed();

            creationTime = currentTimeMillis();
        }
    }



    public GameActivity(){
        gameHistory = new ConcurrentHashMap<>();
        gameAirplanes = new ConcurrentHashMap<>();
        gameCanvas = new GameCanvas();

        gameStartedTime = currentTimeMillis();
    }

    public void setRadar(StackPane newRadar){
        radar = newRadar;
    }

    public void addAirplane(){
        addAirplane(new Airplane(DEFAULT_MAX_SPEED, DEFAULT_MIN_SPEED));
    }

    public void addAirplane(Airplane airplane){
        gameAirplanes.put(airplane.getUid(), airplane);
    }


    public void updateAirplane(Airplane airplane){
        if (!gameAirplanes.containsKey(airplane.getUid())){
            gameAirplanes.put(airplane.getUid(), airplane);
        }
        else{
            gameAirplanes.replace(airplane.getUid(), airplane);
        }
        if(!gameHistory.containsKey(airplane.getUid())){
            gameHistory.put(airplane.getUid(), new ArrayList<>());
        }
        gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
    }

    public void wrapPrinting(){
        gameCanvas.start_printing();
        gameAirplanes.forEach((k, airplane)-> printAirplane(airplane));
        gameHistory.forEach((k, trailDot)-> {
            int trailCounter = 0;
            double x, y;
            for (int j = trailDot.size() - 1;
                 trailCounter <= RADAR_DOTS_HISTORY && j >= 0;
                 trailCounter++, j--) {
                x = trailDot.get(j).xPos;
                y = trailDot.get(j).yPos;
                gameCanvas.printDot(x, y);
            }
        });
        gameCanvas.finish_printing(radar);
    }

    public void printAllDots(){
        gameHistory.forEach((k, trailDots)->{
            double x, y;
            for (TrailDot trailDot : trailDots) {
                x = trailDot.xPos;
                y = trailDot.yPos;
                gameCanvas.printDot(x, y);
            }
        });
    }

    public void printAirplane(Airplane airplane){
        if(gameHistory.containsKey(airplane.getUid())) {
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }
        else{
            gameHistory.put(airplane.getUid(), new ArrayList<>());
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }
        gameCanvas.print_airplane(airplane);
    }

    public void resizeCanvas(){
        gameCanvas.resize_canvas(radar);
    }

    public void moveAirplanes_DEBUG(){
        gameAirplanes.forEach((k, airplane) -> {
            if(new Random().nextBoolean() && new Random().nextBoolean() && new Random().nextBoolean()){
                airplane.setTargetHeading(airplane.getCurrHeading()+(new Random().nextInt(3)-1)*180);
            }
            airplane.moveAirplane();
        });
    }

    public void moveAirplanes_NOCHANGE(){
        gameAirplanes.forEach((k, airplane) -> airplane.moveAirplane());
    }




}
