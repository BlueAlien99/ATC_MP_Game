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

	private static final double climbAccStep = 10;
	private static final double turnAccStep = 0.8;
	private static final double speedAccStep = 0.6;

	private static int numOfAirlines;

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

	static{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream(Dimensions.AIRLINES_FILE);
		Scanner sc = new Scanner(is);

		numOfAirlines = 0;
		while(sc.hasNextLine()){
			sc.nextLine();
			++numOfAirlines;
		}
	}

	public Airplane(UUID owner, double posX, double posY, double altitude, double heading, double speed){
		registerAircraft();
		this.owner = owner;

		this.minSpeed = Dimensions.DEFAULT_MIN_SPEED;
		this.maxSpeed = Dimensions.DEFAULT_MAX_SPEED;

		this.posX = posX;
		this.posY = posY;

		setTargetAltitude(altitude);
		setTargetHeading(heading);
		setTargetSpeed(speed);

		this.altitude = this.targetAltitude;
		this.heading = this.targetHeading;
		this.speed = this.targetSpeed;

		this.altitudeAcceleration = 0;
		this.headingAcceleration = 0;
		this.speedAcceleration = 0;

		this.collisionCourse = false;
		calculateABParams();
	}

	public Airplane(UUID uuid, String callsign, String radarsign, double posX, double posY, double altitude, double heading, double speed){
		this(null, posX, posY, altitude, heading, speed);
		this.uuid = uuid;
		this.callsign = callsign;
		this.radarsign = radarsign;
	}

	//TODO: it should depend on time
	public void moveAirplane(){
		collisionCourse = false;

		if(Dimensions.DEBUGGING_MODE){
			altitude = targetAltitude;
			heading = targetHeading;
			speed = targetSpeed;
		} else{
			updateAltitude();
			updateHeading();
			updateSpeed();
		}

		double headingRad = Math.toRadians(heading);
		double xShift = Math.sin(headingRad) * speed/10;
		double yShift = Math.cos(headingRad) * speed/10;

		posX += xShift;
		posY -= yShift;

		calculateABParams();
	}

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
		if(heading == 0){
			return 360;
		}
		return heading;
	}

	public double getSpeed() {
		return speed;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public double getTargetHeading() {
		if(targetHeading == 0){
			return 360;
		}
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
		} else{
			targetAltitude = Math.min(altitude, Dimensions.AIRPLANE_MAX_ALTITUDE);
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
		} else{
			targetSpeed = Math.min(speed, maxSpeed);
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

	private void updateAltitude(){
		double diff = targetAltitude - altitude;

		if(diff == 0){
			return;
		}

		double absDiff = Math.abs(diff);
		altitudeAcceleration = Math.abs(altitudeAcceleration);

		altitudeAcceleration = Math.min(altitudeAcceleration + climbAccStep, absDiff);
		altitudeAcceleration = Math.min(altitudeAcceleration, Dimensions.AIRPLANE_MAX_CLIMB_RATE);
		altitudeAcceleration *= diff / absDiff;

		altitude += altitudeAcceleration;
	}

	private void updateSpeed(){
		double diff = targetSpeed - speed;

		if(diff == 0){
			return;
		}

		double absDiff = Math.abs(diff);
		speedAcceleration = Math.abs(speedAcceleration);

		speedAcceleration = Math.min(speedAcceleration + speedAccStep, absDiff);
		speedAcceleration = Math.min(speedAcceleration, Dimensions.AIRPLANE_MAX_ACCELERATION);
		speedAcceleration *= diff / absDiff;

		speed += speedAcceleration;
	}

	private void updateHeading(){
		double diff = targetHeading - heading;

		if(diff == 0){
			return;
		}

		if(diff > 180){
			diff -= 360;
		}
		if(diff < -180){
			diff += 360;
		}

		double absDiff = Math.abs(diff);
		headingAcceleration = Math.abs(headingAcceleration);

		headingAcceleration = Math.min(headingAcceleration + turnAccStep, absDiff);
		headingAcceleration = Math.min(headingAcceleration, Dimensions.AIRPLANE_MAX_TURN_RATE);
		headingAcceleration *= diff / absDiff;

		heading += headingAcceleration;

		if(heading < 0){
			heading += 360;
		}
		if(heading >= 360){
			heading -= 360;
		}
	}

	private void registerAircraft() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream(Dimensions.AIRLINES_FILE);
		Scanner sc = new Scanner(is);

		Random rand = new Random();
		int currentLine = 1;
		int targetLine = rand.nextInt(numOfAirlines) + 1;

		while (currentLine != targetLine) {
			sc.nextLine();
			++currentLine;
		}

		String line = sc.nextLine();
		int commaIndex = line.indexOf(',');
		int ident = rand.nextInt(10000);
		radarsign = line.substring(0, commaIndex) + ident;
		callsign = line.substring(commaIndex + 1) + ' ' + ident;

		uuid = UUID.randomUUID();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
