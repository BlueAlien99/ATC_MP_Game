package com.atc.client.model;

public class Checkpoint {
    private final int standardRadius = 50;
    private int points;
    private double xPos;
    private double yPos;
    private double altitude;
    private double radius;

    public Checkpoint(int points) {
        this.radius = calculateRadius(points);
        this.points = points;
    }

    public Checkpoint(double x, double y, int points) {
        this.xPos = x;
        this.yPos = y;
        this.points = points;
        this.radius = calculateRadius(points);
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
}
