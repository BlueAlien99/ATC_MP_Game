package com.atc.client.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static com.atc.client.Dimensions.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class GameCanvas  {
    public Canvas radarAirplanes;
    public Canvas radarTrails;

    public GameCanvas(){
        radarAirplanes = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        radarTrails = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

        /*DEBUG*/
        radarAirplanes.setOnMousePressed(e->System.out.println(e.getX() +" "+e.getY()));
    }

    public void start_printing(){
        GraphicsContext gc = radarAirplanes.getGraphicsContext2D();
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();
        gc.clearRect(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
        gcDots.clearRect(0,0,CANVAS_WIDTH,CANVAS_HEIGHT);
    }

    //this abomination of a function will need some thinking. It works though


    public void print_airplane(Airplane airplane){
        print_airplane(airplane, false);
    }
    public void print_airplane(Airplane airplane, Boolean active){

        double level = airplane.getCurrHeight();
        double targetLevel = airplane.getTargetHeight();
        double speed = airplane.getCurrSpeed();
        double targetSpeed = airplane.getTargetSpeed();
        double hdg = airplane.getCurrHeading();
        double targetHdg = airplane.getTargetHeading();
        double x = airplane.getPositionX();
        double y = airplane.getPositionY();
        String callsign = airplane.getId();

        double x_line = LEADING_LINE_LENGTH*sin(Math.toRadians(hdg));
        double y_line = LEADING_LINE_LENGTH*cos(Math.toRadians(hdg));

        String alt_symbol = "=";
        if(level < targetLevel) alt_symbol="↑";
        if(level > targetLevel) alt_symbol="↓";

        String speed_symbol="=";
        if(speed < targetSpeed) speed_symbol="↑";
        if(speed > targetSpeed) speed_symbol="↓";

        String hdg_symbol="=";
        if((Math.abs(hdg-targetHdg)>180 && targetHdg<hdg) ||
                (Math.abs(hdg-targetHdg)<=180 && targetHdg>hdg)) hdg_symbol="→";
        if((Math.abs(hdg-targetHdg)>180 && targetHdg>hdg) ||
                (Math.abs(hdg-targetHdg)<=180 && targetHdg<hdg)) hdg_symbol="←";


        GraphicsContext gc = radarAirplanes.getGraphicsContext2D();
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();

        gc.setFill(active ? RADAR_ACTIVE_COLOR : RADAR_COLOR);
        gc.setStroke(active ? RADAR_ACTIVE_COLOR : RADAR_COLOR);

        gcDots.setFill(active ? RADAR_ACTIVE_COLOR : RADAR_COLOR);

        gc.strokeText(callsign, x-15, y-43);

        gc.strokeText(String.format("%03d",Math.round(level))
                        + alt_symbol +
                        String.format("%03d",Math.round(targetLevel))
                        + "FL",
                x-15, y-32);

        gc.strokeText(String.format("%03d",Math.round(speed))
                        + speed_symbol +
                        String.format("%03d",Math.round(targetSpeed))
                        + "KTS",
                x-15, y-21);

        gc.strokeText(String.format("%03d", ((Math.round(hdg)) != 360) ? Math.round(hdg) : 0)
                        + hdg_symbol +
                        String.format("%03d", ((Math.round(targetHdg)) != 360) ? Math.round(targetHdg) : 0),
                x-15, y-10);

        gc.setLineWidth(1);
        gc.strokeLine(x, y, x+x_line, y-y_line);
        gc.fillRect(x-5,y-5,10,10);

        gc.setFill(Color.BLACK);
        gc.fillRect(x-3, y-3, 6, 6);
    }

    void printDot(double x, double y){
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();
        gcDots.setFill(RADAR_COLOR);
        gcDots.fillOval(x, y, 2, 2);
    }

    public void finish_printing(StackPane radar){
        radar.getChildren().clear();
        radar.getChildren().add(radarTrails);
        radar.getChildren().add(radarAirplanes);
    }

    public void resize_canvas(StackPane radar){
        radarAirplanes.setScaleX(radar.getWidth() / CANVAS_WIDTH);
        radarAirplanes.setScaleY(radar.getHeight() / CANVAS_HEIGHT);
        radarTrails.setScaleX(radar.getWidth() / CANVAS_WIDTH);
        radarTrails.setScaleY(radar.getHeight() / CANVAS_HEIGHT);

    }

    public void print_airplanes_array(ArrayList<Airplane> airplanes, StackPane radar){
        start_printing();
        for(Airplane airplane : airplanes){
            print_airplane(airplane);
        }
        finish_printing(radar);
    }

}
