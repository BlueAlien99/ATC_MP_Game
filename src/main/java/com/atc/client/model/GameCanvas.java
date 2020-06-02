package com.atc.client.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.UUID;

import static com.atc.client.Dimensions.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class GameCanvas extends StackPane {
    private Canvas radarAirplanes;
    private Canvas radarCheckpoints;
    private Canvas radarTrails;

    public GameCanvas(){
        radarAirplanes = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        radarTrails = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        radarCheckpoints = new Canvas(CANVAS_WIDTH,CANVAS_HEIGHT);
    }

    @Override
    public void setPrefSize(double prefWidth, double prefHeight){
        super.setPrefSize(prefWidth, prefHeight);
        resize_canvas();
    }

    public void start_printing(){
        GraphicsContext gc = radarAirplanes.getGraphicsContext2D();
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();
        GraphicsContext gcCheckpoints = radarCheckpoints.getGraphicsContext2D();
        gc.clearRect(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
        gcDots.clearRect(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
        gcCheckpoints.setFill(RADAR_BACKGROUND);
        gcCheckpoints.fillRect(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
    }

    public void removeAirplanes(){
        GraphicsContext gc = radarAirplanes.getGraphicsContext2D();
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();
        gc.clearRect(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
        gcDots.clearRect(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
    }

    //TODO: this abomination of a function will need some thinking. It works though

    public void print_airplane(Airplane airplane){
        print_airplane(airplane, false, false);
    }
    public void print_airplane(Airplane airplane, Boolean active){
        print_airplane(airplane, active, false);
    }
    public void print_airplane(Airplane airplane, Boolean active, Boolean ownership){

        double altitude = airplane.getAltitude();
        double targetAltitude = airplane.getTargetAltitude();
        double speed = airplane.getSpeed();
        double targetSpeed = airplane.getTargetSpeed();
        double heading = airplane.getHeading();
        double targetHeading = airplane.getTargetHeading();
        double x = airplane.getPosX();
        double y = airplane.getPosY();
        String callsign = airplane.getRadarsign();

        boolean collisionCourse = airplane.isCollisionCourse() && (ownership || DEBUGGING_MODE);
        boolean crashed = airplane.isCrashed() && (ownership || DEBUGGING_MODE);

        if(active)
        System.out.println(airplane.getPosX() + "    " + airplane.getPosY());

        double x_line = LEADING_LINE_LENGTH*sin(Math.toRadians(heading));
        double y_line = LEADING_LINE_LENGTH*cos(Math.toRadians(heading));

        if(DEBUGGING_MODE) {
            x_line = speed * sin(Math.toRadians(heading));
            y_line = speed * cos(Math.toRadians(heading));
        }

        String alt_symbol = "=";
        if(altitude < targetAltitude) alt_symbol="↑";
        if(altitude > targetAltitude) alt_symbol="↓";

        String speed_symbol="=";
        if(speed < targetSpeed) speed_symbol="↑";
        if(speed > targetSpeed) speed_symbol="↓";

        String hdg_symbol="=";
        if((Math.abs(heading-targetHeading)>180 && targetHeading<heading) ||
                (Math.abs(heading-targetHeading)<=180 && targetHeading>heading)) hdg_symbol="→";
        if((Math.abs(heading-targetHeading)>180 && targetHeading>heading) ||
                (Math.abs(heading-targetHeading)<=180 && targetHeading<heading)) hdg_symbol="←";

        GraphicsContext gc = radarAirplanes.getGraphicsContext2D();
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();

        Paint radarPaint = crashed ? RADAR_CRASHED_COLOR : active ? RADAR_ACTIVE_COLOR : collisionCourse ? RADAR_COLLISION_COLOR : ownership ? RADAR_USER_COLOR : RADAR_COLOR;

        gc.setFill(radarPaint);
        gc.setStroke(radarPaint);

        gcDots.setFill(active ? RADAR_ACTIVE_COLOR : RADAR_COLOR);

        gc.strokeText(callsign, x-15, y-43);

        gc.strokeText(String.format("%03d",Math.round(altitude))
                        + alt_symbol +
                        String.format("%03d",Math.round(targetAltitude))
                        + "FT",
                x-15, y-32);

        gc.strokeText(String.format("%03d",Math.round(speed))
                        + speed_symbol +
                        String.format("%03d",Math.round(targetSpeed))
                        + "KTS",
                x-15, y-21);

        gc.strokeText(String.format("%03d", ((Math.round(heading)) != 360) ? Math.round(heading) : 0)
                        + hdg_symbol +
                        String.format("%03d", ((Math.round(targetHeading)) != 360) ? Math.round(targetHeading) : 0),
                x-15, y-10);

        gc.setLineWidth(1);
        gc.strokeLine(x, y, x+x_line, y-y_line);
        gc.fillRect(x-5,y-5,10,10);

        gc.clearRect(x-2.5, y-2.5, 5, 5);
//        gcDots.clearRect(x-3, y-3, 6, 6);
    }

    void printDot(double x, double y){
        printDot(x, y, RADAR_COLOR);
    }

    void printDot(double x, double y, Paint dotColor){
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();
        gcDots.setFill(dotColor);
        gcDots.fillOval(x, y, 2, 2);
    }
    public void printCheckpoint(Checkpoint checkpoint, UUID activeAirplane){
        GraphicsContext gcCheckpoints = radarCheckpoints.getGraphicsContext2D();
        double radius = checkpoint.getRadius();
        System.out.println("Currently active: "+activeAirplane);
        System.out.println(checkpoint.getAirplane(activeAirplane));
        if(checkpoint.getAirplane(activeAirplane)) {
            gcCheckpoints.setFill(CHECKPOINT_PASSED);
        }
        else {
            gcCheckpoints.setFill(CHECKPOINT_NORMAL);
        }
        gcCheckpoints.setGlobalAlpha(CHECKPOINT_ALPHA);
        gcCheckpoints.fillOval(checkpoint.getxPos()-radius/2,
                checkpoint.getyPos()-radius/2, radius, radius);
        gcCheckpoints.setGlobalAlpha(1);
    }

    public void printCheckpoint(Checkpoint checkpoint) {
        printCheckpoint(checkpoint, null);
    }

    public void finish_printing(){
        this.getChildren().clear();
        this.getChildren().add(radarCheckpoints);
        this.getChildren().add(radarTrails);
        this.getChildren().add(radarAirplanes);
    }

    public void resize_canvas(){
        radarAirplanes.setScaleX(xCoeff());
        radarAirplanes.setScaleY(yCoeff());
        radarTrails.setScaleX(xCoeff());
        radarTrails.setScaleY(yCoeff());
        radarCheckpoints.setScaleX(xCoeff());
        radarCheckpoints.setScaleY(yCoeff());
    }

    public double xCoeff(){ return this.getPrefWidth() / CANVAS_WIDTH; }

    public double yCoeff(){
        return this.getPrefHeight() / CANVAS_HEIGHT;
    }

    public void print_airplanes_array(ArrayList<Airplane> airplanes, StackPane radar){
        start_printing();
        for(Airplane airplane : airplanes){
            print_airplane(airplane);
        }
        finish_printing();
    }

}
