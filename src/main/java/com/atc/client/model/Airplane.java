package com.atc.client.model;

import com.atc.client.Dimensions;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import static com.atc.client.Dimensions.SIM_TICK_DELAY;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Class representing airplanes in game.
 */
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
	private double lastPosX;
	private double lastPosY;

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
	private boolean crashed;

	static{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream(Dimensions.AIRLINES_FILE);
		assert is != null;
		Scanner sc = new Scanner(is);

		numOfAirlines = 0;
		while(sc.hasNextLine()){
			sc.nextLine();
			++numOfAirlines;
		}
	}

	/**
	 * Instantiates a new Airplane.
	 *
	 * @param owner    the owner
	 * @param posX     the pos x
	 * @param posY     the pos y
	 * @param altitude the altitude
	 * @param heading  the heading
	 * @param speed    the speed
	 */
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
		this.crashed = false;
		calculateABParams();
	}

	/**
	 * Instantiates a new Airplane.
	 *
	 * @param uuid      the uuid
	 * @param callsign  the callsign
	 * @param radarsign the radarsign
	 * @param posX      the pos x
	 * @param posY      the pos y
	 * @param altitude  the altitude
	 * @param heading   the heading
	 * @param speed     the speed
	 */
	public Airplane(UUID uuid, String callsign, String radarsign, double posX, double posY, double altitude, double heading, double speed){
		this(null, posX, posY, altitude, heading, speed);
		this.uuid = uuid;
		this.callsign = callsign;
		this.radarsign = radarsign;
	}

	/**
	 * Default method to move airplane by one step. Used in game to move airplanes.
	 */
	public void moveAirplane(){
		moveAirplane(1);
	}

	/**
	 * Moves airplanes by given number of steps. Used in collision detection and spawning airplanes on canvas at specified time.
	 *
	 * @param steps indicates how many steps we want our airplane to make
	 */
	public void moveAirplane(double steps){
		if(crashed){
			return;
		}

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
		double xShift = steps * Math.sin(headingRad) * speed/10 * SIM_TICK_DELAY/1000;
		double yShift = steps * Math.cos(headingRad) * speed/10 * SIM_TICK_DELAY/1000;

		// Used to calculate distance between a trajectory of an airplane and a checkpoint
		lastPosX = posX;
		lastPosY = posY;

		posX += xShift;
		posY -= yShift;

		calculateABParams();
	}

	/**
	 * Gets callsign.
	 *
	 * @return the callsign
	 */
	public String getCallsign() {
		return callsign;
	}

	/**
	 * Gets radarsign.
	 *
	 * @return the radarsign
	 */
	public String getRadarsign() {
		return radarsign;
	}

	/**
	 * Gets uuid.
	 *
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Gets owner.
	 *
	 * @return the owner
	 */
	public UUID getOwner() {
		return owner;
	}

	/**
	 * Gets pos x.
	 *
	 * @return the pos x
	 */
	public double getPosX() {
		return posX;
	}

	/**
	 * Sets pos x.
	 *
	 * @param posX the pos x
	 */
	public void setPosX(double posX) {
		this.posX = posX;
	}

	/**
	 * Gets pos y.
	 *
	 * @return the pos y
	 */
	public double getPosY() {
		return posY;
	}

	/**
	 * Set pos y.
	 *
	 * @param posY the pos y
	 */
	public void setPosY(double posY){
		this.posY = posY;
	}

	/**
	 * Gets last pos x.
	 *
	 * @return the last pos x
	 */
	public double getLastPosX() {
		return lastPosX;
	}

	/**
	 * Gets last pos y.
	 *
	 * @return the last pos y
	 */
	public double getLastPosY() {
		return lastPosY;
	}

	/**
	 * Gets altitude.
	 *
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Gets heading.
	 *
	 * @return the heading
	 */
	public double getHeading() {
		if(heading == 0){
			return 360;
		}
		return heading;
	}

	/**
	 * Gets speed.
	 *
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Gets target altitude.
	 *
	 * @return the target altitude
	 */
	public double getTargetAltitude() {
		return targetAltitude;
	}

	/**
	 * Gets target heading.
	 *
	 * @return the target heading
	 */
	public double getTargetHeading() {
		if(targetHeading == 0){
			return 360;
		}
		return targetHeading;
	}

	/**
	 * Gets target speed.
	 *
	 * @return the target speed
	 */
	public double getTargetSpeed() {
		return targetSpeed;
	}

	/**
	 * Sets target params.
	 *
	 * @param speed    the speed
	 * @param heading  the heading
	 * @param altitude the altitude
	 */
	public void setTargetParams(double speed, double heading, double altitude){
		setTargetAltitude(altitude);
		setTargetHeading(heading);
		setTargetSpeed(speed);
	}

	/**
	 * Set target altitude and corrects it if user set it wrongly in the chat.
	 *
	 * @param altitude the altitude
	 */
	public void setTargetAltitude(double altitude){
		if(altitude < Dimensions.AIRPLANE_MIN_ALTITUDE){
			targetAltitude = Dimensions.AIRPLANE_MIN_ALTITUDE;
		} else{
			targetAltitude = Math.min(altitude, Dimensions.AIRPLANE_MAX_ALTITUDE);
		}
	}

	/**
	 * Sets target heading.
	 *
	 * @param heading the heading
	 */
	public void setTargetHeading(double heading) {
		heading %= 360;

		if(heading < 0){
			heading -= 360 * Math.floor(heading / 360);
		}

		targetHeading = heading;
	}

	/**
	 * Sets target speed and corrects it if user set it wrong in the chat.
	 *
	 * @param speed the speed
	 */
	public void setTargetSpeed(double speed){
		if(speed < minSpeed){
			targetSpeed = minSpeed;
		} else{
			targetSpeed = Math.min(speed, maxSpeed);
		}
	}

	/**
	 * Calculates ab params.
	 */
	public void calculateABParams(){
		double x = posX + speed * sin(Math.toRadians(heading));
		double y = posY + speed * cos(Math.toRadians(heading));
		// Mathematically speaking, in the expression below there should NOT be - sign,
		// but strangely enough JavaFX's canvas has mirrored y axis.
		colAParam = - (y - posY) / (x - posX);
		colBParam = posY - (colAParam * posX);
	}

	/**
	 * Gets col a param.
	 *
	 * @return the col a param
	 */
	public double getColAParam() {
		return colAParam;
	}

	/**
	 * Gets col b param.
	 *
	 * @return the col b param
	 */
	public double getColBParam() {
		return colBParam;
	}

	/**
	 * Checks if airplane is on collision course.
	 *
	 * @return the boolean
	 */
	public boolean isCollisionCourse() {
		return collisionCourse;
	}

	/**
	 * Sets collision course.
	 */
	public void setCollisionCourse() {
		this.collisionCourse = true;
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed() {
		this.crashed = true;
	}

	/**
	 * Updates airplane's altitude
	 */
	private void updateAltitude(){
		double diff = targetAltitude - altitude;

		if(diff == 0){
			altitudeAcceleration = 0;
			return;
		}

		double absDiff = Math.abs(diff);
		altitudeAcceleration = Math.abs(altitudeAcceleration);

		altitudeAcceleration = Math.min(altitudeAcceleration + climbAccStep * SIM_TICK_DELAY/1000, absDiff);
		altitudeAcceleration = Math.min(altitudeAcceleration, Dimensions.AIRPLANE_MAX_CLIMB_RATE * SIM_TICK_DELAY/1000);
		altitudeAcceleration *= diff / absDiff;

		altitude += altitudeAcceleration;
	}

	/**
	 * Updates airplane's speed
	 */
	private void updateSpeed(){
		double diff = targetSpeed - speed;

		if(diff == 0){
			speedAcceleration = 0;
			return;
		}

		double absDiff = Math.abs(diff);
		speedAcceleration = Math.abs(speedAcceleration);

		speedAcceleration = Math.min(speedAcceleration + speedAccStep * SIM_TICK_DELAY/1000, absDiff);
		speedAcceleration = Math.min(speedAcceleration, Dimensions.AIRPLANE_MAX_ACCELERATION * SIM_TICK_DELAY/1000);
		speedAcceleration *= diff / absDiff;

		speed += speedAcceleration;
	}

	/**
	 * Updates airplane's heading
	 */
	private void updateHeading(){
		double diff = targetHeading - heading;

		if(diff == 0){
			headingAcceleration = 0;
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

		headingAcceleration = Math.min(headingAcceleration + turnAccStep * SIM_TICK_DELAY/1000, absDiff);
		headingAcceleration = Math.min(headingAcceleration, Dimensions.AIRPLANE_MAX_TURN_RATE * SIM_TICK_DELAY/1000);
		headingAcceleration *= diff / absDiff;

		heading += headingAcceleration;

		if(heading < 0){
			heading += 360;
		}
		if(heading >= 360){
			heading -= 360;
		}
	}

	/**
	 * Gives airplane its callsign and radarsign - it simply randomly chooses it from the list.
	 */
	private void registerAircraft() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream(Dimensions.AIRLINES_FILE);
		assert is != null;
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
