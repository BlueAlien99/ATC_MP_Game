package com.atc.client;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Dimensions {

    public static final boolean DEBUGGING_MODE = true;

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;

    public static final double CANVAS_HEIGHT = 1000;
    public static final double CANVAS_WIDTH = 1000;

    public static final double DEFAULT_MIN_SPEED = 160;
    public static final double DEFAULT_MAX_SPEED = 500;

    public static final double LEADING_LINE_LENGTH = 20;

    public static final int RADAR_DOTS_HISTORY = 5;

    public static final Paint RADAR_COLOR = Color.LIMEGREEN;
    public static final Paint RADAR_ACTIVE_COLOR = Color.BLUE;
    public static final Paint RADAR_COLLISION_COLOR = Color.RED;
//    public static final Paint RADAR_BACKGROUND = Color.DARKBLUE;
    public static final Paint RADAR_BACKGROUND = Color.BLACK;

    public static final double AIRPLANE_MIN_ALTITUDE = 1000;
    public static final double AIRPLANE_MAX_ALTITUDE = 40000;

    public static final String AIRLINES_FILE = "csv/airlines.csv";
}
