package com.atc.client.model;

import com.atc.client.Dimensions;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Airplane implements Cloneable, Serializable {

	private String callsign;
	private String radarsign;
	private UUID uuid;
	private UUID owner;

	private double minSpeed;
	private double maxSpeed;

	private double posX;
	private double posY;

	private double altitude;
	private double heading;
	private double speed;

	private double altitudeAcceleration;
	private double headingAcceleration;
	private double speedAcceleration;

	private double targetAltitude;
	private double targetHeading;
	private double targetSpeed;

	private double colAParam;
	private double colBParam;
	private boolean collisionCourse;

/*
	private UUID owner;

	private double minSpeed;
	private double maxSpeed;

	*/


	public String getCallsign() {
		return callsign;
	}

	public String getRadarsign() {
		return radarsign;
	}

	public UUID getUuid() {
		return uuid;
	}

	public UUID getOwner() {
		return owner;
	}

	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) throws Exception {
		if(Dimensions.DEBUGGING_MODE){
			this.posX = posX;
		} else{
			throw new Exception("This method requires DEBUGGING_MODE to be enabled!");
		}
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) throws Exception {
		if(Dimensions.DEBUGGING_MODE) {
			this.posY = posY;
		} else{
			throw new Exception("This method requires DEBUGGING_MODE to be enabled!");
		}
	}

	public double getAltitude() {
		return altitude;
	}

	public double getHeading() {
		return heading;
	}

	public double getSpeed() {
		return speed;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public double getTargetHeading() {
		return targetHeading;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}

	public void setTargetParams(double speed, double heading, double altitude){
		setTargetAltitude(altitude);
		setTargetHeading(heading);
		setTargetSpeed(speed);
	}

	public void setTargetAltitude(double altitude){
		if(altitude < Dimensions.AIRPLANE_MIN_ALTITUDE){
			targetAltitude = Dimensions.AIRPLANE_MIN_ALTITUDE;
		}
		else if(altitude > Dimensions.AIRPLANE_MAX_ALTITUDE){
			targetAltitude = Dimensions.AIRPLANE_MAX_ALTITUDE;
		}
		else{
			targetAltitude = altitude;
		}
	}

	public void setTargetHeading(double heading) {
		heading %= 360;

		if(heading < 0){
			heading -= 360 * Math.floor(heading / 360);
		}

		targetHeading = heading;
	}

	public void setTargetSpeed(double speed){
		if(speed < minSpeed){
			targetSpeed = minSpeed;
		}
		else if(speed > maxSpeed){
			targetSpeed = maxSpeed;
		}
		else{
			targetSpeed = speed;
		}
	}

	public void calculateABParams(){
		double x = posX + speed * sin(Math.toRadians(heading));
		double y = posY + speed * cos(Math.toRadians(heading));
		// Mathematically speaking, in the expression below there should NOT be - sign,
		// but strangely enough JavaFX's canvas has mirrored y axis.
		colAParam = - (y - posY) / (x - posX);
		colBParam = posY - (colAParam * posX);
	}

	public double getColAParam() {
		return colAParam;
	}

	public double getColBParam() {
		return colBParam;
	}

	public boolean isCollisionCourse() {
		return collisionCourse;
	}

	public void setCollisionCourse() {
		this.collisionCourse = true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE
	// OLD CODE - OLD CODE - OLD CODE

	public Airplane(double initialMaxSpeed, double initialMinSpeed){
		this.uuid = UUID.randomUUID();
		this.callsign = generateAirplaneId(1000,2);
		this.speed =0;
		this.targetSpeed = 0;
		this.heading = 0;
		this.targetHeading = 0;
		this.altitude = 0;
		this.targetHeading =0;
		this.posX = 0;
		this.posY = 0;
		this.maxSpeed = initialMaxSpeed;
		this.minSpeed = initialMinSpeed;
	}

	public Airplane(UUID owner, double heading){
		this(100, 10);
		this.owner = owner;
		this.targetSpeed = 50;
		this.targetAltitude = 10000;
		this.targetHeading = heading;
	}

	public Airplane(UUID owner, double heading, double mins, double maxs, double pox, double poy){
		this(owner, heading);
		this.maxSpeed = maxs;
		this.minSpeed = mins;
		this.posX = pox;
		this.posY = poy;
	}

	public void moveAirplane(){
		collisionCourse = false;
		setNewFlightParameters();
		double radians = Math.toRadians(heading);
		double xShift = Math.sin(radians) * speed/10;
		double yShift = Math.cos(radians)* speed/10;
		posX += xShift;
		posY -= yShift;
	}

	private void setNewFlightParameters(){
		//updateSpeed();
		//updateHeading();
		speed = targetSpeed;
		heading = targetHeading;
		altitude = targetAltitude;
//		updateHeight();
		calculateABParams();
	}

	private void updateSpeed(){
		final int speedStep = 10;

		//UPDATING SPEED
		double difference = targetSpeed - speed;
		if(difference > 0 && difference > speedStep) {
			speed += speedStep;
		}else if (difference < 0 && Math.abs(difference) > speedStep){
			speed -= speedStep;
		}else if (Math.abs((difference)) > 0){
			speed = targetSpeed;
		}
	}
	private void updateHeading(){
		final int headingStep = 15;

		double difference = targetHeading - heading;
		if(difference != 0) {
			if (Math.abs(difference) > 180) {
				if (difference > 0) {
					if (difference > 345) {
						heading = targetHeading;
					} else
						heading -= headingStep;
				} else {
					if (difference < -345) {
						heading = targetHeading;
					} else
						heading += headingStep;
				}
			} else if (difference > 0 && difference > headingStep) {
				heading += headingStep;
			} else if (difference < 0 && Math.abs(difference) > headingStep) {
				heading -= headingStep;
			} else if ((Math.abs(difference)) % 360 <= headingStep) {
				heading = targetHeading;
			}
			calculateABParams();
		}
	}



	private void updateHeight(){
		final int heightStep = 10;

		double difference = targetAltitude - altitude;
		if(difference > 0 && difference > heightStep){
			altitude += heightStep;
		}else if(difference < 0 && Math.abs(difference) > heightStep){
			altitude -= heightStep;
		}else if (Math.abs((difference)) > 0){
			altitude = targetAltitude;
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


	/*static{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream("csv/airlines.csv");
		Scanner sc = new Scanner(is);


	}

	private void registerAircraft(){
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream("csv/airlines.csv");
		Scanner sc = new Scanner(is);
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String iataAirline = line.substring(0, Utils.findNthOccurance(line, ',', 1));
	}*/


}
