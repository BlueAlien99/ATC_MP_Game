package com.atc.client.model;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static com.atc.client.GlobalConsts.*;

/**
 * Class representing checkpoints in game.
 */
public class Checkpoint implements Serializable, Cloneable {
    private final static int standardRadius = 50;
    private UUID checkpointUUID;
    private int gameID;
    private int points;
    private double xPos;
    private double yPos;
    private double radius;

    /**
     * Every checkpoint has his own checkpoint with airplanes to check whether it passed it or not.
     */
    private ConcurrentHashMap<UUID, Boolean> airplanes = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Checkpoint - used only to make tests.
     */
    public Checkpoint() {}

    /**
     * Instantiates a new Checkpoint.
     *
     * @param points the points
     */
    public Checkpoint(int points) {
        this.radius = calculateRadius(points);
        this.points = points;
        checkpointUUID = UUID.randomUUID();
    }

    /**
     * Instantiates a new Checkpoint.
     *
     * @param x      the x
     * @param y      the y
     * @param points the points
     */
    public Checkpoint(double x, double y, int points) {
        this.xPos = x;
        this.yPos = y;
        this.points = points;
        this.radius = calculateRadius(points);
        checkpointUUID = UUID.randomUUID();
    }

    /**
     * Instantiates a new Checkpoint.
     *
     * @param checkpointUUID the checkpoint uuid
     * @param gameID         the game id
     * @param points         the points
     * @param xPos           the x pos
     * @param yPos           the y pos
     */
    public Checkpoint(UUID checkpointUUID, int gameID, int points, double xPos,
                      double yPos) {
    this.checkpointUUID = checkpointUUID;
    this.gameID = gameID;
    this.points = points;
    this.xPos = xPos;
    this.yPos = yPos;
    this.radius = calculateRadius(points);
    }

    /**
     * Checks if airplane passed a checkpoint.
     *
     * @param airplane the airplane
     * @return true if airplane passed, false otherwise
     */
    public boolean checkAirplane(Airplane airplane) {
        if(airplanes.get(airplane.getUuid())==null) {
            addAirplane(airplane.getUuid());
        }
        if(!getAirplane(airplane.getUuid()) && calculateDistanceCheckAirplane(airplane)<=Math.pow(radius/2, 2)) {
            airplanes.put(airplane.getUuid(), true);
            return true;
        }
        return false;
    }

    /**
     * Checks if airplane with given UUID is in checkpoint's hashmap.
     *
     * @param airplaneUUID the airplane UUID
     * @return  true if airplane is on the list, false otherwise
     */
    public boolean getAirplane(UUID airplaneUUID) {
        if(airplaneUUID == null)
            return false;
        if(airplanes.get(airplaneUUID)!=null){
            return airplanes.get(airplaneUUID);
        }
        return false;
    }


    /**
     * Marks airplane on checkpoint's hashmap as passed.
     *
     * @param airplaneUUID the airplane uuid
     */
    public void passAirplane(UUID airplaneUUID){
        //System.out.println(airplaneUUID+" passes checkpoint");
        airplanes.put(airplaneUUID, true);

        //System.out.println("Pass result: " + getAirplane(airplaneUUID));

    }

    /**
     * Add an airplane to checkpoint's hashmap.
     *
     * @param airplaneUUID the airplane uuid
     */
    public void addAirplane(UUID airplaneUUID){
        airplanes.put(airplaneUUID, false);
    }

    /**
     * Method to calculate distance between a checkpoint (seen as a point) and airplane trajectory
     * (seen as a segment).
     * @param airplane
     * x - position x of a checkpoint
     * y - position y of a checkpoint
     * x1 - position x of last step of an airplane
     * y1 - position y of last step of an airplane
     * x2 - actual x position of an airplane
     * y2 - actual y position of an airplane
     * u - parameter used to indicate a point in a segment that has the smallest distance to checkpoint.
     * @return distance between an airplane and a checkpoint
     */
    private double calculateDistanceCheckAirplane(Airplane airplane){
        double x = xPos;
        double y = yPos;
        double x1 = airplane.getLastPosX();
        double x2 = airplane.getPosX();
        double y1 = airplane.getLastPosY();
        double  y2 = airplane.getPosY();
        double u = ((x2-x1)*(x-x1) + (y2-y1)*(y-y1))/(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
        if(u<0){
            return Math.pow((x1-x),2) + Math.pow((y1-y),2);
        }else if( u>0 && u<1){
            double x3 = x1 + u*(x2 - x1);
            double y3 = y1 + u*(y2 - y1);
            return Math.pow((x3-x),2) + Math.pow((y3-y),2);
        }else{
            return Math.pow((x2-x),2) + Math.pow((y2-y),2);
        }
    }

    /**
     * Calculate radius of checkpoint. The more points it has, the smaller it gets.
     * @param points Value of a checkpoint in points
     * @return radius of a checkpoint
     */

    public double calculateRadius(int points) {

        return 10 * ((double)standardRadius*POINTS_MULTIPLIER/points);
    }


    /**
     * Gets points.
     *
     * @return the points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Gets pos.
     *
     * @return the pos
     */
    public double getxPos() {
        return xPos;
    }

    /**
     * Gets pos.
     *
     * @return the pos
     */
    public double getyPos() {
        return yPos;
    }

    /**
     * Gets radius.
     *
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Gets checkpoint uuid.
     *
     * @return the checkpoint uuid
     */
    public UUID getCheckpointUUID() {
        return checkpointUUID;
    }

    /**
     * Gets game id.
     *
     * @return the game id
     */
    public int getGameID() {
        return gameID;
    }

    public ConcurrentHashMap<UUID, Boolean> getAirplanes() {
        return airplanes;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
