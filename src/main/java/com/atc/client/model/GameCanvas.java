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
        public ArrayList<Canvas> radarDotsArray;

    public GameCanvas(){
        radarAirplanes = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        radarDotsArray = new ArrayList<>();
        for(int i = 0; i<RADAR_DOTS_LAYERS; i++){
            radarDotsArray.add(new Canvas(CANVAS_WIDTH,CANVAS_HEIGHT));
        }
    }

    public void rotate_dots(){
        radarDotsArray.remove(0);
        radarDotsArray.add(new Canvas(CANVAS_WIDTH,CANVAS_HEIGHT));
    }

    public void start_printing(){
        GraphicsContext gc = radarAirplanes.getGraphicsContext2D();
        gc.clearRect(0,0,800,800);
        rotate_dots();
    }

    public void print_airplane(double x, double y,
                               double hdg, double targetHdg,
                               double level, double targetLevel,
                               double speed, double targetSpeed,
                               String callsign){

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
        GraphicsContext gc_dots = radarDotsArray.get(radarDotsArray.size() - 1).getGraphicsContext2D();

        gc.setFill(Color.LIMEGREEN);
        gc.setStroke(Color.LIMEGREEN);

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

        gc_dots.setFill(Color.LIMEGREEN);
        gc_dots.fillOval(x, y, 2, 2);
    }

    public void finish_printing(StackPane radar){
        radar.getChildren().clear();
        radarAirplanes.setScaleX(radar.getWidth() / CANVAS_WIDTH);
        radarAirplanes.setScaleY(radar.getHeight() / CANVAS_HEIGHT);
        for (Canvas canvas : radarDotsArray) {
            canvas.setScaleX(radar.getWidth() / CANVAS_WIDTH);
            canvas.setScaleY(radar.getHeight() / CANVAS_HEIGHT);
            radar.getChildren().add(canvas);
        }
        radar.getChildren().add(radarAirplanes);
    }

    public void resize_canvas(StackPane radar){
        radarAirplanes.setScaleX(radar.getWidth() / CANVAS_WIDTH);
        radarAirplanes.setScaleY(radar.getHeight() / CANVAS_HEIGHT);
        for (Canvas canvas : radarDotsArray) {
            canvas.setScaleX(radar.getWidth() / CANVAS_WIDTH);
            canvas.setScaleY(radar.getHeight() / CANVAS_HEIGHT);
        }

    }

    public void print_airplanes_array(ArrayList<Airplane> airplanes, StackPane radar){
        start_printing();
        for(Airplane airplane : airplanes){
            print_airplane(airplane.getPositionX(), airplane.getPositionY(),
                    airplane.getCurrHeading(), airplane.getTargetHeading(),
                    airplane.getCurrHeight(), airplane.getTargetHeight(),
                    airplane.getCurrSpeed(), airplane.getTargetSpeed(),
                    airplane.getId());
        }
        finish_printing(radar);
    }

}
