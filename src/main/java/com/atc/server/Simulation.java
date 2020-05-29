package com.atc.server;

import com.atc.client.model.Airplane;
import com.atc.client.model.TCAS;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.Dimensions.CANVAS_HEIGHT;
import static com.atc.client.Dimensions.CANVAS_WIDTH;

public class Simulation extends TimerTask {

    private GameState gameState;
    private ConcurrentHashMap<String, Airplane> airplanes;

    public Simulation(GameState gameState) {
        this.gameState = gameState;
        this.airplanes = gameState.getAirplanes();
    }

    @Override
    public void run() {
        if(!gameState.simulationPaused())
            return;
        airplanes.forEach((k, v) -> {
            v.moveAirplane();
            //This is just for testing, so that user does not loose airplanes
            try {
                if (v.getPosX() < 0) v.setPosX(CANVAS_WIDTH);
                if (v.getPosY() < 0) v.setPosY(CANVAS_HEIGHT);
                if (v.getPosX() > CANVAS_WIDTH) v.setPosX(0);
                if (v.getPosY() > CANVAS_HEIGHT) v.setPosY(0);
                //used for collision detection, remove when 4 ifs above are removed
                v.calculateABParams();
            } catch (Exception ex){
                ex.printStackTrace();
            }

            gameState.getLog().insertEvent(
                    gameState.getGameCount(), "MOVEMENT", gameState.getTickCount(), v.getOwner(),
                    gameState.searchPlayerLogin(v.getOwner()),
                    v.getPosX(), v.getPosY(), v.getSpeed(), v.getHeading(),
                    v.getAltitude(), v.getUuid());
        });

        TCAS.calculateCollisions(airplanes);

//            gameState.getLog().commit();
        gameState.setNewAirplanesOutput();
        System.out.println("1s has passed");
        gameState.simulationResume();
    }
}
