package com.atc.client.model;
import java.util.*;

public class Airplane {
    private String id;
    private double curr_speed;
    private double target_speed;
    private double curr_heading;
    private double target_heading;
    private double curr_height;
    private double target_height;
    private double curr_pos_x;
    private double curr_pos_y;
    private double max_speed;
    private double min_speed;

    public String get_id(){ return this.id;}
    public double get_curr_speed(){
        return this.curr_speed;
    }
    public double get_target_speed(){return this.target_speed;}
    public double get_curr_heading(){ return this.curr_heading; }
    public double get_target_heading(){return this.target_heading;}
    public double get_curr_height(){
        return this.curr_height;
    }
    public double get_target_height(){ return this.target_height;}
    public double get_position_x(){ return this.curr_pos_x; }
    public double get_position_y(){
        return this.curr_pos_y;
    }
    public double get_min_speed(){
        return  this.min_speed;
    }
    public double get_max_speed(){return this.max_speed;}

    public void set_id(String new_id){
        this.id = new_id;
    }
    public void set_curr_speed(double new_speed){
        this.curr_speed = new_speed;
    }
    public void set_target_speed(double new_target_speed){
        this.target_speed = new_target_speed;
    }
    public void set_curr_heading(double new_heading) {
        this.curr_heading = new_heading;
    }
    public void set_target_heading(double new_target_heading){
        this.target_heading = new_target_heading;
    }
    public void set_curr_height(double new_curr_height){
        this.curr_height = new_curr_height;
    }
    public void set_target_height(double new_target_height){
        this.target_height = new_target_height;
    }
    public void set_curr_pos_x(double new_pos_x) {
        this.curr_pos_x = new_pos_x;
    }
    public void set_curr_pos_y(double new_pos_y){
        this.curr_pos_y = new_pos_y;
    }
    public void set_max_speed(double new_max_speed){
        this.max_speed = new_max_speed;
    }
    public void set_min_speed(double new_min_speed){
        this.min_speed = new_min_speed;
    }

}
