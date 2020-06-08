package com.atc.client.model;

import com.atc.client.controller.GameActivityController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.GlobalConsts.RADAR_DOTS_HISTORY;
import static java.lang.System.currentTimeMillis;

/**
 * Container class that keeps necessary information to display current state of game on GameCanvas.
 */
public class GameActivity {

    private long gameStartedTime;
    private Map<UUID, ArrayList<TrailDot>> gameHistory;
    private Map<UUID, Airplane> gameAirplanes;
    private ConcurrentHashMap<UUID, Checkpoint> checkpoints = new ConcurrentHashMap<>();

    public GameCanvas radar;

    private UUID clientUUID;
    private UUID activeAirplane;

    private GameActivityController gameActivityController;

    /**
     * Gets checkpoints.
     *
     * @return the checkpoints
     */
    public ConcurrentHashMap<UUID, Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    /**
     * Adds checkpoints to GameActivity.
     *
     * @param checkpoints the checkpoints
     */
    public void setCheckpoints(ConcurrentHashMap<UUID, Checkpoint> checkpoints) {
        checkpoints.forEach(((uuid, checkpoint) -> this.checkpoints.put(uuid, checkpoint)));
        //System.out.println("SET CHECKPOINTS");
        checkpoints.forEach(((uuid, checkpoint) -> {
            //System.out.println(uuid);
            //checkpoint.getAirplanes().forEach(((uuid1, aBoolean) -> System.out.println(uuid1.toString() + aBoolean)));
        }));
        //System.out.println("END SET CHECKPOINTS");
    }


    /**
     * Class representing trailing dots.
     */
    static class TrailDot{
        public double xPos;
        public double yPos;
        public double heading;
        public double altitude;
        public double speed;

        public long creationTime;

        /**
         * Instantiates a new Trail dot.
         *
         * @param airplane the airplane
         */
        TrailDot(Airplane airplane){
            xPos = airplane.getPosX();
            yPos = airplane.getPosY();
            heading = airplane.getHeading();
            altitude = airplane.getAltitude();
            speed = airplane.getSpeed();

            creationTime = currentTimeMillis();
        }
    }

    /**
     * Instantiates a new Game activity.
     *
     * @param controller the controller
     */
    public GameActivity(GameActivityController controller){
        gameHistory = new ConcurrentHashMap<>();
        gameAirplanes = new ConcurrentHashMap<>();
        radar = new GameCanvas();

        gameStartedTime = currentTimeMillis();

        activeAirplane = null;

        gameActivityController = controller;
    }

    /**
     * Sets radar.
     *
     * @param newRadar the new radar
     */
    public void setRadar(GameCanvas newRadar){
        radar = newRadar;
    }

    /**
     * Sets client uuid.
     *
     * @param clientUUID the client uuid
     */
    public void setClientUUID(UUID clientUUID){
        this.clientUUID = clientUUID;
    }

    /**
     * Adds airplane.
     *
     * @param airplane the airplane
     */
    public void addAirplane(Airplane airplane){
        gameAirplanes.put(airplane.getUuid(), airplane);
    }

    /**
     * Updates airplanes - replaces old airplanes with new ones from GameActivity and adds trail dot for it.
     *
     * @param airplanes the airplane
     */
    public void updateAirplanes(ConcurrentHashMap<UUID, Airplane> airplanes){
        gameAirplanes = airplanes;
        airplanes.forEach((k,v) -> {
			if(!gameHistory.containsKey(k)){
				gameHistory.put(k, new ArrayList<>());
			}
			gameHistory.get(k).add(new TrailDot(v));
        });

        gameHistory.forEach((k, v) -> {
            if (gameAirplanes.get(k) == null) {
                gameHistory.remove(k);
            }
        });
    }

    /**
     * Wrap printing - used in GameActivity to print airplanes and trailing dots.
     */
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

    /**
     * Prints airplane.
     *
     * @param airplane the airplane
     */
    public void printAirplane(Airplane airplane){
        /*
        if(gameHistory.containsKey(airplane.getUid())) {
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }
        else{
            gameHistory.put(airplane.getUid(), new ArrayList<>());
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }*/

        radar.print_airplane(airplane, airplane.getUuid() == activeAirplane, airplane.getOwner() != null && airplane.getOwner().equals(clientUUID));

    }

    /**
     * Resizes canvas.
     */
    public void resizeCanvas(){
        radar.resize_canvas();
    }

    /**
     * Method only used in debugging mode to move a random airplane in random direction.
     */
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

    /**
     * Gets the closest airplane to mouse click.
     * @param x - x position of mouse click
     * @param y - y position of mouse click
     * @return uuid of the nearest airplane
     */
    private UUID getClosest(double x, double y){
        double min = 2048;
        UUID ret = null;
        for (Map.Entry<UUID, Airplane> pair: gameAirplanes.entrySet()) {
            if(pair.getValue().getOwner() != null && pair.getValue().getOwner().equals(clientUUID)) {
                double val = (pair.getValue().getPosY() - y) * (pair.getValue().getPosY() - y)
                        + (pair.getValue().getPosX() - x) * (pair.getValue().getPosX() - x);
                if (val < min) {
                    min = val;
                    ret = pair.getValue().getUuid();
                }
            }
        }

        //System.out.println(x+" "+y+" "+ret);
        return ret;

    }

    /**
     * Sets the closest airplane as active.
     *
     * @param x the x
     * @param y the y
     */
    public void setActive(double x, double y){
        activeAirplane = getClosest(x,y);
        updateChatBoxes();
    }

    /**
     * Updates chat boxes after user sending new targets for the airplane.
     */
    public void updateChatBoxes(){
        if(activeAirplane != null){
            Airplane plane = gameAirplanes.get(activeAirplane);
            gameActivityController.updateChatBoxes(plane.getTargetHeading(), plane.getTargetSpeed(), plane.getTargetAltitude());
        } else{
            gameActivityController.clearChatBoxes();
        }
    }

    /**
     * Gets an active airplane's uuid.
     *
     * @return the uuid
     */
    public UUID getActiveAirplane(){
        return activeAirplane;
    }

    /**
     * Gets airplane by uuid airplane.
     *
     * @param uuid the uuid
     * @return the airplane
     */
    public Airplane getAirplaneByUUID(UUID uuid){
        return gameAirplanes.get(uuid);
    }

}
