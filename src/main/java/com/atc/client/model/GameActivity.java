package com.atc.client.model;

import com.atc.client.controller.GameActivityController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.Dimensions.RADAR_DOTS_HISTORY;
import static java.lang.System.currentTimeMillis;

public class GameActivity {

    private long gameStartedTime;
    private Map<UUID, ArrayList<TrailDot>> gameHistory;
    private Map<UUID, Airplane> gameAirplanes;
    private ConcurrentHashMap<UUID, Checkpoint> checkpoints = new ConcurrentHashMap<>();

    public GameCanvas radar;

    private UUID clientUUID;
    private UUID activeAirplane;

    private GameActivityController gameActivityController;

    public ConcurrentHashMap<UUID, Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(ConcurrentHashMap<UUID, Checkpoint> checkpoints) {
        checkpoints.forEach(((uuid, checkpoint) -> {
            this.checkpoints.put(uuid, checkpoint);
        }));
        System.out.println("SET CHECKPOINTS");
        checkpoints.forEach(((uuid, checkpoint) -> {
            System.out.println(uuid);
            checkpoint.airplanes.forEach(((uuid1, aBoolean) -> {
                System.out.println(uuid1.toString() + aBoolean);
            }));
        }));
        System.out.println("END SET CHECKPOINTS");
    }


    static class TrailDot{
        public double xPos;
        public double yPos;
        public double heading;
        public double altitude;
        public double speed;

        public long creationTime;

        TrailDot(Airplane airplane){
            xPos = airplane.getPosX();
            yPos = airplane.getPosY();
            heading = airplane.getHeading();
            altitude = airplane.getAltitude();
            speed = airplane.getSpeed();

            creationTime = currentTimeMillis();
        }
    }

    public GameActivity(GameActivityController controller){
        gameHistory = new ConcurrentHashMap<>();
        gameAirplanes = new ConcurrentHashMap<>();
        radar = new GameCanvas();

        gameStartedTime = currentTimeMillis();

        activeAirplane = null;

        gameActivityController = controller;
    }

    public void setRadar(GameCanvas newRadar){
        radar = newRadar;
    }

    public void setClientUUID(UUID clientUUID){
        this.clientUUID = clientUUID;
    }

    public void addAirplane(Airplane airplane){
        gameAirplanes.put(airplane.getUuid(), airplane);
    }

    public void updateAirplane(Airplane airplane){
        if (!gameAirplanes.containsKey(airplane.getUuid())){
            gameAirplanes.put(airplane.getUuid(), airplane);
        }
        else{
            gameAirplanes.replace(airplane.getUuid(), airplane);
        }
        if(!gameHistory.containsKey(airplane.getUuid())){
            gameHistory.put(airplane.getUuid(), new ArrayList<>());
        }
        gameHistory.get(airplane.getUuid()).add(new TrailDot(airplane));
    }

    public void wrapPrinting(){
        radar.start_printing();
        gameAirplanes.forEach((k, airplane)-> printAirplane(airplane));
        gameHistory.forEach((k, trailDot)-> {
            int trailCounter = 0;
            double x, y;
            for (int j = trailDot.size() - 2;
                 trailCounter <= RADAR_DOTS_HISTORY && j >= 0;
                 trailCounter++, j--) {
                x = trailDot.get(j).xPos;
                y = trailDot.get(j).yPos;
                radar.printDot(x, y);
            }
        });
        checkpoints.forEach((uuid, checkpoint) -> radar.printCheckpoint(checkpoint, activeAirplane));
        radar.finish_printing();

    }

    public void printAllDots(){
        gameHistory.forEach((k, trailDots)->{
            double x, y;
            for (TrailDot trailDot : trailDots) {
                x = trailDot.xPos;
                y = trailDot.yPos;
                radar.printDot(x, y);
            }
        });
    }

    public void printAirplane(Airplane airplane){
        /*
        if(gameHistory.containsKey(airplane.getUid())) {
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }
        else{
            gameHistory.put(airplane.getUid(), new ArrayList<>());
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }*/

        radar.print_airplane(airplane, airplane.getUuid() == activeAirplane, airplane.getOwner().equals(clientUUID));

    }

    public void resizeCanvas(){
        radar.resize_canvas();
    }

    public void moveAirplanes_DEBUG(){
        gameAirplanes.forEach((k, airplane) -> {
            if(new Random().nextBoolean() && new Random().nextBoolean() && new Random().nextBoolean()){
                airplane.setTargetHeading(airplane.getHeading()+(new Random().nextInt(3)-1)*180);
            }
            airplane.moveAirplane();
        });
    }

    public void moveAirplanes_NOCHANGE(){
        gameAirplanes.forEach((k, airplane) -> airplane.moveAirplane());
    }

    private UUID getClosest(double x, double y){
        double min = 2048;
        UUID ret = null;
        for (Map.Entry<UUID, Airplane> pair: gameAirplanes.entrySet()) {
            if(pair.getValue().getOwner().equals(clientUUID)) {
                double val = (pair.getValue().getPosY() - y) * (pair.getValue().getPosY() - y)
                        + (pair.getValue().getPosX() - x) * (pair.getValue().getPosX() - x);
                if (val < min) {
                    min = val;
                    ret = pair.getValue().getUuid();
                }
            }
        }

        System.out.println(x+" "+y+" "+ret);
        return ret;

    }

    public void setActive(double x, double y){
        activeAirplane = getClosest(x,y);
        updateChatBoxes();
    }

    public void updateChatBoxes(){
        if(activeAirplane != null){
            Airplane plane = gameAirplanes.get(activeAirplane);
            gameActivityController.updateChatBoxes(plane.getTargetHeading(), plane.getTargetSpeed(), plane.getTargetAltitude());
        } else{
            gameActivityController.clearChatBoxes();
        }
    }

    public UUID getActiveAirplane(){
        return activeAirplane;
    }

    public Airplane getAirplaneByUUID(UUID uuid){
        return gameAirplanes.get(uuid);
    }

}
