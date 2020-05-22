package com.atc.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Airplane implements Cloneable, Serializable {

    private UUID uid;
    private String id;
    private UUID owner;
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

    private boolean collisionCourse;
    private double colAParam;
    private double colBParam;

    public Airplane(double initialMaxSpeed, double initialMinSpeed){
        this.uid = UUID.randomUUID();
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
//        this.collisionCourse = new ArrayList<Airplane>();
    }

    public Airplane(UUID owner, double heading){
        this(100, 10);
        this.owner = owner;
        this.targetSpeed = 50;
        this.targetHeight = 10000;
        this.targetHeading = heading;
//        this.collisionCourse = new ArrayList<Airplane>();
    }

    @Override
    public String toString() {
        return "Airplane{" +
                "id='" + id + '\'' +
                ", owner='" + owner.toString() + '\'' +
                ", currSpeed=" + currSpeed +
                ", currHeading=" + currHeading +
                ", currHeight=" + currHeight +
                ", xPos=" + currPosX +
                ", yPos=" + currPosY +
                '}';
    }

    public void moveAirplane(){
        collisionCourse = false;
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

    private void setNewFlightParameters(){
        updateSpeed();
        updateHeading();
        updateHeight();
        calculateABParams();
    }

    private void updateSpeed(){
        final int speedStep = 10;

        //UPDATING SPEED
        double currSpeed = getCurrSpeed();
        double targetSpeed = getTargetSpeed();
        double difference = targetSpeed - currSpeed;
        if(difference > 0 && difference > speedStep) {
            setCurrSpeed(currSpeed + speedStep);
        }else if (difference < 0 && Math.abs(difference) > speedStep){
            setCurrSpeed(currSpeed - speedStep);
        }else if (Math.abs((difference)) > 0){
            setCurrSpeed(getTargetSpeed());
        }
    }
    private void updateHeading(){
        final int headingStep = 15;

        double currHeading = getCurrHeading();
        double targetHeading = getTargetHeading();
        double difference = targetHeading - currHeading;
        if(difference != 0) {
            if (Math.abs(difference) > 180) {
                if (difference > 0) {
                    if (difference > 345) {
                        setCurrHeading(targetHeading);
                    } else
                        setCurrHeading(currHeading - headingStep);
                } else {
                    if (difference < -345) {
                        setCurrHeading(targetHeading);
                    } else
                        setCurrHeading(currHeading + headingStep);
                }
            } else if (difference > 0 && difference > headingStep) {
                setCurrHeading(currHeading + headingStep);
            } else if (difference < 0 && Math.abs(difference) > headingStep) {
                setCurrHeading((currHeading - headingStep));
            } else if ((Math.abs(difference)) % 360 <= headingStep) {
                setCurrHeading(getTargetHeading());
            }
            calculateABParams();
        }
    }

    public void calculateABParams(){
        double x1 = currPosX + currSpeed*sin(Math.toRadians(currHeading));
        double y1 = currPosY + currSpeed*cos(Math.toRadians(currHeading));
        colAParam = (y1 - currPosY) / (x1 - currPosX);
        colBParam = currPosY - (colAParam * currPosX);
    }

    private void updateHeight(){
        final int heightStep = 10;

        double currHeight = getCurrHeight();
        double targetHeight = getTargetHeight();
        double difference = targetHeight - currHeight;
        if(difference > 0 && difference > heightStep){
            setCurrHeight(currHeight + heightStep);
        }else if(difference < 0 && Math.abs(difference) > heightStep){
            setCurrHeight(currHeight - heightStep);
        }else if (Math.abs((difference)) > 0){
            setCurrHeight(getTargetHeight());
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
        id.append(r.nextInt(upper));
        //GENERATE LETTERS IN AIRPLANE ID
        return id.toString();
    }


    public UUID getUid(){return this.uid; }
    public String getId(){ return this.id;}
    public double getCurrSpeed(){ return this.currSpeed; }
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
        if(newSpeed < getMaxSpeed() && newSpeed > getMinSpeed()) {
            this.currSpeed = newSpeed;
        }
    }
    public void setTargetSpeed(double newTargetSpeed){
        if (newTargetSpeed < getMaxSpeed() && newTargetSpeed > getMinSpeed()) {
            this.targetSpeed = newTargetSpeed;
        }
    }
    public void setCurrHeading(double newHeading) {
        if(newHeading > 360) {
            this.currHeading = newHeading - 360;
        }else if (newHeading < 0 ){
            this.currHeading = newHeading + 360;
        } else{
            this.currHeading = newHeading;
        }
    }
    public void setTargetHeading(double newTargetHeading){
        if(newTargetHeading > 360) {
            setTargetHeading(newTargetHeading-360);
        }else if (newTargetHeading < 0){
            setTargetHeading(newTargetHeading+360);
        }else {
            this.targetHeading = newTargetHeading;
        }
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

    public UUID getOwner() {
        return owner;
    }

    public void setNewTargets(double speed, double heading, double height){
        targetSpeed = speed;
        targetHeading = heading;
        targetHeight = height;
    }

    public void setCollisionCourse(){
        collisionCourse = true;
    }

    public boolean getCollisionCourse(){
        return collisionCourse;
    }

    public double getColAParam() {
        return colAParam;
    }

    public double getColBParam() {
        return colBParam;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
