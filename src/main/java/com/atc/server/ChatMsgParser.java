package com.atc.server;

import com.atc.client.model.Airplane;

public class ChatMsgParser {

    public static String parseNewMsg(Airplane oldPlane, Airplane newPlane){

        double speedDiff = newPlane.getTargetSpeed() - oldPlane.getTargetSpeed();
        double headingDiff = newPlane.getTargetHeading() - oldPlane.getTargetHeading();
        double altitudeDiff = newPlane.getTargetAltitude() - oldPlane.getTargetAltitude();

        if(speedDiff == 0 && headingDiff == 0 && altitudeDiff == 0){
            return null;
        }

        String msg = newPlane.getCallsign();

        if(headingDiff != 0){
            msg += ", fly heading " + (int)newPlane.getTargetHeading();
        }
        if(altitudeDiff < 0){
            msg += ", descend to " + (int)newPlane.getTargetAltitude() + " feet";
        } else if(altitudeDiff > 0){
            msg += ", climb to " + (int)newPlane.getTargetAltitude() + " feet";
        }
        if(speedDiff != 0){
            msg += ", speed " + (int)newPlane.getTargetSpeed() + " knots";
        }

        msg = msg + '.';

        return msg;
    }
}
