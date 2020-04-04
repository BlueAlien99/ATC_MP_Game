package com.atc.client.controller;
import com.atc.client.model.Airplane;
import java.util.*;

public class AirplaneController {
    private Airplane model;

    public void AirplaneController(Airplane airplane) {
        this.model = airplane;
    }

    public void move_airplane(){
        set_new_flight_parameters();
        double curr_speed = get_airplane_curr_speed();
        double curr_pos_x = get_airplane_position_x();
        double curr_pos_y = get_airplane_position_y();
        double radians = Math.toRadians(get_airplane_curr_heading());
        double x_shift = Math.sin(radians) * curr_speed/10;
        double y_shift = Math.cos(radians)* curr_speed/10;
        set_airplane_curr_pos_x(curr_pos_x + x_shift);
        set_airplane_curr_pos_y(curr_pos_y + y_shift);
    }

    public void set_new_flight_parameters(){

        final int speed_step = 10;
        final int heading_step = 5;
        final int height_step = 10;

        //UPDATING SPEED
        double curr_speed = get_airplane_curr_speed();
        double target_speed = get_airplane_target_speed();
        double difference = target_speed - curr_speed;
        if(difference > 0) {
            set_airplane_curr_speed(curr_speed + speed_step);
        }else if (difference < 0){
            model.set_curr_speed(curr_speed - speed_step);
        }
        //UPDATING HEADING
        double curr_heading = get_airplane_curr_heading();
        double target_heading = get_airplane_target_heading();
        difference = target_heading - curr_heading;
        if(difference > 0){
            model.set_curr_heading(curr_heading + heading_step);
        }else if (difference < 0){
            model.set_curr_heading((curr_heading - heading_step));
        }
        //UPDATING HEIGHT
        double curr_height = get_airplane_curr_height();
        double target_height = get_airplane_target_height();
        difference = target_height - curr_height;
        if(difference > 0){
            model.set_curr_height(curr_height + height_step);
        }else if(difference < 0){
            model.set_curr_height(curr_height + height_step);
        }
    }
    private String generate_airplane_id(int upper, int length){
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

    public String get_airplane_id() {return this.model.get_id();}
    public double get_airplane_curr_speed() {return this.model.get_curr_speed(); }
    public double get_airplane_target_speed(){return this.model.get_target_speed(); }
    public double get_airplane_curr_heading() {return this.model.get_curr_heading(); }
    public double get_airplane_target_heading(){return this.model.get_target_heading(); }
    public double get_airplane_curr_height() {return this.model.get_curr_height();}
    public double get_airplane_target_height(){return this.model.get_target_height();}
    public double get_airplane_position_x() {return this.model.get_position_x(); }
    public double get_airplane_position_y() {return this.model.get_position_y(); }
    public double get_airplane_min_speed() {return this.model.get_min_speed();}
    public double get_airplane_max_speed() {return this.model.get_max_speed(); }


    public void set__airplane_id() {
        this.model.set_id(generate_airplane_id(1000, 2));
    }

    public void set_airplane_target_speed(double new_target_speed){
        this.model.set_target_speed(new_target_speed);
    }
    public void set_airplane_curr_speed(double new_curr_speed){
        this.model.set_curr_speed(new_curr_speed);
    }
    public void set_airplane_target_heading(double new_target_heading){
        this.model.set_target_heading(new_target_heading);
    }
    public void set_airplane_curr_heading(double new_heading) {
        this.model.set_curr_heading(new_heading);
    }

    public void set_airplane_curr_height(double new_curr_height) {
        this.model.set_curr_height(new_curr_height);
    }
    public void set_airplane_target_height(double new_target_height){
        this.model.set_target_height(new_target_height);
    }
    public void set_airplane_curr_pos_x(double new_pos_x) {
        this.model.set_curr_pos_x(new_pos_x);
    }

    public void set_airplane_curr_pos_y(double new_pos_y) {
        this.model.set_curr_pos_y(new_pos_y);
    }

    private void set_airplane_max_speed(double new_max_speed) {
        this.model.set_max_speed(new_max_speed);
    }

    private void set_airplane_min_speed(double new_min_speed) {
        this.set_airplane_min_speed(new_min_speed);
    }
}
