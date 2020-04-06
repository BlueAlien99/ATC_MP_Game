package com.atc.server;

import com.atc.client.model.Airplane;

public class ChatMsgParser {

    public static String parseNewMsg(Airplane oldPlane, Airplane newPlane){

        double speedDiff = newPlane.getTargetSpeed() - oldPlane.getTargetSpeed();
        double headingDiff = newPlane.getTargetHeading() - oldPlane.getTargetHeading();
        double heightDiff = newPlane.getTargetHeight() - oldPlane.getTargetHeight();

        String msg = newPlane.getId() + ", ";

        if(speedDiff != 0){
            msg += "speed " + newPlane.getTargetSpeed() + " (" + speedDiff + "), ";
        }
        if(heightDiff != 0){
            msg += "height " + newPlane.getTargetHeight() + " (" + heightDiff + "), ";
        }
        if(headingDiff != 0){
            msg += "heading " + newPlane.getTargetHeading() + " (" + headingDiff + "), ";
        }
        return msg;
    }
}
