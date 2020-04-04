package com.atc.client.model;
import java.util.*;

public class Airplane {
    private String id;
    private double currSpeed;
    private double targetSpeed;
    private double currHeading;
    private double targetHeading;
    private double currHeight;
    private double targetHeight;
    private double currPosX;
    private double currPosY;
    private double maxSpeed;
    private double minSpeed;

    Airplane(double initialMaxSpeed, double initialMinSpeed){
        this.id = generateAirplaneId(1000,2);
        this.currSpeed =0;
        this.targetSpeed = 0;
        this.currHeading = 0;
        this.targetHeading = 0;
        this.currHeight = 0;
        this.targetHeading =0;
        this.currPosX = 0;
        this.currPosY = 0;
        this.maxSpeed = initialMaxSpeed;
        this.minSpeed = initialMinSpeed;
    }


    public void moveAirplane(){
        setNewFlightParameters();
        double currSpeed = getCurrSpeed();
        double currPosX = getPositionX();
        double currPosY = getPositionY();
        double radians = Math.toRadians(getCurrHeading());
        double xShift = Math.sin(radians) * currSpeed/10;
        double yShift = Math.cos(radians)* currSpeed/10;
        setCurrPosX(currPosX + xShift);
        setCurrPosY(currPosY - yShift);
    }

    public void setNewFlightParameters(){

        final int speedStep = 10;
        final int headingStep = 5;
        final int heightStep = 10;

        //UPDATING SPEED
        double currSpeed = getCurrSpeed();
        double targetSpeed = getTargetSpeed();
        double difference = targetSpeed - currSpeed;
        if(difference > 0 && difference > speedStep) {
            setCurrSpeed(currSpeed + speedStep);
        }else if (difference < 0 && difference > speedStep){
            setCurrSpeed(currSpeed - speedStep);
        }
        //UPDATING HEADING
        double currHeading = getCurrHeading();
        double targetHeading = getTargetHeading();
        difference = targetHeading - currHeading;
        if(difference > 0 && difference > headingStep){
            setCurrHeading(currHeading + headingStep);
        }else if (difference < 0 && difference > headingStep){
            setCurrHeading((currHeading - headingStep));
        }
        //UPDATING HEIGHT
        double currHeight = getCurrHeight();
        double targetHeight = getTargetHeight();
        difference = targetHeight - currHeight;
        if(difference > 0 && difference > heightStep){
            setCurrHeight(currHeight + heightStep);
        }else if(difference < 0 && difference > heightStep){
            setCurrHeight(currHeight + heightStep);
        }
    }
    private String generateAirplaneId(int upper, int length){
        //GENERATE TWO RANDOM NUMBERS
        StringBuilder id = new StringBuilder();
        Random r = new Random();
        int bound = 26;
        for(int i =0; i< length; i++){
            id.append((char) (r.nextInt(bound) + 'A'));
        }
        id.append(" ");
        id.append(r.nextInt(upper));
        //GENERATE LETTERS IN AIRPLANE ID
        return id.toString();
    }

    public String getId(){ return this.id;}
    public double getCurrSpeed(){
        return this.currSpeed;
    }
    public double getTargetSpeed(){return this.targetSpeed;}
    public double getCurrHeading(){ return this.currHeading; }
    public double getTargetHeading(){return this.targetHeading;}
    public double getCurrHeight(){ return this.currHeight; }
    public double getTargetHeight(){ return this.targetHeight;}
    public double getPositionX(){ return this.currPosX; }
    public double getPositionY(){
        return this.currPosY;
    }
    public double getMinSpeed(){
        return  this.minSpeed;
    }
    public double getMaxSpeed(){return this.maxSpeed;}

    public void setId(String newId){
        this.id = newId;
    }
    public void setCurrSpeed(double newSpeed){
        this.currSpeed = newSpeed;
    }
    public void setTargetSpeed(double newTargetSpeed){
        this.targetSpeed = newTargetSpeed;
    }
    public void setCurrHeading(double newHeading) {
        this.currHeading = newHeading;
    }
    public void setTargetHeading(double newTargetHeading){
        this.targetHeading = newTargetHeading;
    }
    public void setCurrHeight(double newCurrHeight){
        this.currHeight = newCurrHeight;
    }
    public void setTargetHeight(double newTargetHeight){
        this.targetHeight = newTargetHeight;
    }
    public void setCurrPosX(double newPosX) {
        this.currPosX = newPosX;
    }
    public void setCurrPosY(double newPosY){
        this.currPosY = newPosY;
    }
    public void setMaxSpeed(double newMaxSpeed){
        this.maxSpeed = newMaxSpeed;
    }
    public void setMinSpeed(double newMinSpeed){
        this.minSpeed = newMinSpeed;
    }

}