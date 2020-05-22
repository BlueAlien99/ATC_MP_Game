package com.atc.server;

import com.atc.client.model.Airplane;

public class ChatMsgParser {

    public static String parseNewMsg(Airplane oldPlane, Airplane newPlane){

        double speedDiff = newPlane.getTargetSpeed() - oldPlane.getTargetSpeed();
        double headingDiff = newPlane.getTargetHeading() - oldPlane.getTargetHeading();
        double altitudeDiff = newPlane.getTargetHeight() - oldPlane.getTargetHeight();

        if(speedDiff == 0 && headingDiff == 0 && altitudeDiff == 0){
            return null;
        }

        String msg = newPlane.getId();

        if(headingDiff != 0){
            msg += ", fly heading " + newPlane.getTargetHeading();
        }
        if(altitudeDiff < 0){
            msg += ", descend to " + newPlane.getTargetHeight() + " feet";
        } else if(altitudeDiff > 0){
            msg += ", climb to " + newPlane.getTargetHeight() + " feet";
        }
        if(speedDiff != 0){
            msg += ", speed " + newPlane.getTargetSpeed() + " knots";
        }

        msg = msg + '.';

        return msg;
    }
}
