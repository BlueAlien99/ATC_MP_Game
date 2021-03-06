package com.atc.client;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Container class with all the constants used in application
 */
public class GlobalConsts {

    public static final boolean DEBUGGING_MODE = false;

    public static final int SIM_TICK_DELAY = 1000;

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;

    public static final double CANVAS_HEIGHT = 1000;
    public static final double CANVAS_WIDTH = 1000;

    public static final double DEFAULT_MIN_SPEED = 160;
    public static final double DEFAULT_MAX_SPEED = 350;

    public static final double LEADING_LINE_LENGTH = 20;

    public static final int RADAR_DOTS_HISTORY = 5;

    public static final Paint RADAR_COLOR = Color.LIGHTGREY;
    public static final Paint RADAR_USER_COLOR = Color.LAWNGREEN;
    public static final Paint RADAR_ACTIVE_COLOR = Color.BLUE;
    public static final Paint RADAR_COLLISION_COLOR = Color.RED;
    public static final Paint RADAR_CRASHED_COLOR = Color.DARKRED;
    public static final Paint RADAR_BACKGROUND = Color.BLACK;

    public static final Paint CHECKPOINT_NORMAL = Color.YELLOW;
    public static final Paint CHECKPOINT_ACTIVE = Color.BLUE;
    public static final Paint CHECKPOINT_PASSED = Color.GREEN;
    public static final double CHECKPOINT_ALPHA = 0.60;

    public static final double AIRPLANE_MIN_ALTITUDE = 1000;
    public static final double AIRPLANE_MAX_ALTITUDE = 40000;

    // feet per second
    public static final double AIRPLANE_AVG_CLIMB_RATE = 30;
    public static final double AIRPLANE_MAX_CLIMB_RATE = 50;

    // deg per second
    // data gathered experimentally from X-Plane 11
    // Boeing 737-800, 240 IAS at FL60, 180 deg turn with AP
    // avg for 20 deg of bank angle, max for 30 deg of bank angle
    public static final double AIRPLANE_AVG_TURN_RATE = 1.44;
//    public static final double AIRPLANE_MAX_TURN_RATE = 2.25;

    // temporary
    public static final double AIRPLANE_MAX_TURN_RATE = 18;

    public static final double AIRPLANE_MAX_ACCELERATION = 3.2;

    public static final int TIMEOUT_TIME = 100;

    public static final int TIMEOUT_TRIES = 50;

    public static final int AI_GENERATION_FREQUENCY = 12;
    public static final int MAX_AI_PLANES = 6;

    public static final int MAX_CHECKPOINT = 5;
    public static final int MINIMUM_CHECKPOINTS_POINTS = 30;
    public static final int MAXIMUM_CHECKPOINTS_POINTS = 30;
    public static final double POINTS_MULTIPLIER = 10.0;

    public static final int COLLISION_PENALTY = -10;

    public static final String AIRLINES_FILE = "csv/airlines.csv";
}
