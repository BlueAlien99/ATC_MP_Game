package com.atc.client.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.atc.client.Dimensions.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.System.currentTimeMillis;

public class GameCanvas  {
        public Canvas radarAirplanes;
        public Canvas radarTrails;

        private Map<UUID, ArrayList<TrailDot>> gameHistory;

    class TrailDot{
            public double xPos;
            public double yPos;
            public double heading;
            public double altitude;
            public double speed;

            public long creationTime;

            TrailDot(Airplane airplane){
                xPos = airplane.getPositionX();
                yPos = airplane.getPositionY();
                heading = airplane.getCurrHeading();
                altitude = airplane.getCurrHeight();
                speed = airplane.getCurrSpeed();

                creationTime = currentTimeMillis();
            }
    }

    public GameCanvas(){
        radarAirplanes = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        radarTrails = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gameHistory = new HashMap<>();
    }

    public void start_printing(){
        GraphicsContext gc = radarAirplanes.getGraphicsContext2D();
        GraphicsContext gcDots = radarTrails.getGraphicsContext2D();
        gc.clearRect(0,0,800,800);
        gcDots.clearRect(0,0,800,800);
    }

    //this abomination of a function will need some thinking. It works though
    public void print_airplane(Airplane airplane){
        if(gameHistory.containsKey(airplane.getUid())) {
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }
        else{
            gameHistory.put(airplane.getUid(), new ArrayList<>());
            gameHistory.get(airplane.getUid()).add(new TrailDot(airplane));
        }

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

        gc.setFill(RADAR_COLOR);
        gc.setStroke(RADAR_COLOR);

        gcDots.setFill(RADAR_COLOR);

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

        int trailCounter = 0;

        for(int j = gameHistory.get(airplane.getUid()).size()-1; trailCounter<=RADAR_DOTS_HISTORY && j>=0; trailCounter++, j--) {
            x = gameHistory.get(airplane.getUid()).get(j).xPos;
            y = gameHistory.get(airplane.getUid()).get(j).yPos;
            gcDots.fillOval(x, y, 2, 2);
        }
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
