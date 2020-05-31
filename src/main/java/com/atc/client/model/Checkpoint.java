package com.atc.client.model;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Checkpoint implements Serializable, Cloneable {
    private final int standardRadius = 50;
    private UUID checkpointUUID;
    private int points;
    private double xPos;
    private double yPos;
    private double altitude;
    private double radius;

    //TODO: TO BE PRIVATE
    public ConcurrentHashMap<UUID, Boolean> airplanes = new ConcurrentHashMap<>();

    public Checkpoint(int points) {
        this.radius = calculateRadius(points);
        this.points = points;
        checkpointUUID = UUID.randomUUID();
    }

    public Checkpoint(double x, double y, int points) {
        this.xPos = x;
        this.yPos = y;
        this.points = points;
        this.radius = calculateRadius(points);
        checkpointUUID = UUID.randomUUID();
    }

    public boolean checkAirplane(Airplane airplane) {
        if(airplanes.get(airplane.getUuid())==null) {
            addAirplane(airplane.getUuid());
        }
        if(!getAirplane(airplane.getUuid()) && Math.pow(airplane.getPosX()-xPos, 2)+Math.pow(airplane.getPosY()-yPos, 2)<=Math.pow(radius/2, 2)) {
            airplanes.put(airplane.getUuid(), true);
            return true;
        }
        return false;
    }

    public boolean getAirplane(UUID airplaneUUID) {
        if(airplaneUUID == null)
            return false;
        if(airplanes.get(airplaneUUID)!=null){
            return airplanes.get(airplaneUUID);
        }
        return false;
    }


    public void passAirplane(UUID airplaneUUID){
        System.out.println(airplaneUUID+" passes checkpoint");
        airplanes.put(airplaneUUID, true);

        System.out.println("Pass result: " + getAirplane(airplaneUUID));

    }

    public void addAirplane(UUID airplaneUUID){
        airplanes.put(airplaneUUID, false);
    }

    public boolean checkAllAirplanes(){
        for(Map.Entry<UUID, Boolean> pair : airplanes.entrySet()){
            if(!pair.getValue())
                return false;
        }
        return true;
    }

    private double calculateRadius(int points) {
        return 10 * (standardRadius/points);
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getxPos() {
        return xPos;
    }

    public void setxPos(double xPos) {
        this.xPos = xPos;
    }

    public double getyPos() {
        return yPos;
    }

    public void setyPos(double yPos) {
        this.yPos = yPos;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public UUID getCheckpointUUID() {
        return checkpointUUID;
    }

    public void setCheckpointUUID(UUID checkpointUUID) {
        this.checkpointUUID = checkpointUUID;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
