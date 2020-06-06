package com.atc.server;

import com.atc.client.model.Airplane;
import com.atc.client.model.Checkpoint;
import com.atc.client.model.GameSettings;
import com.atc.client.model.TCAS;
import com.atc.server.model.Event;

import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.Dimensions.CANVAS_HEIGHT;
import static com.atc.client.Dimensions.CANVAS_WIDTH;

/**
 * Class with simulation thread that manages movements of airplanes on canvas,
 * detects collisions and checks for passing checkpoint.
 */
public class Simulation extends TimerTask {

    private GameState gameState;
    private ConcurrentHashMap<UUID, Airplane> airplanes;
    private ConcurrentHashMap<UUID, Checkpoint> checkpoints;

    public Simulation(GameState gameState) {
        this.gameState = gameState;
        this.airplanes = gameState.getAirplanes();
        this.checkpoints = gameState.getCheckpoints();
    }

    @Override
    public void run() {
        if(!gameState.simulationPaused()){
            return;
        }

        airplanes.forEach((k, v) -> {
            if(v.isCrashed()){
                gameState.generateNewAirplanes(1, v.getOwner());
                airplanes.remove(k);
            }
            //Used to calculate distance between a trajectory of airplane and checkpoint
            v.setLastPosX(v.getPosX());
            v.setLastPosY(v.getPosY());

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

            checkpoints.forEach((ck, cv) -> {
                if(cv.checkAirplane(v)){
                    System.out.println(v.getUuid()+" JUST PASSED CHECKPOINT "+cv.getCheckpointUUID());
                    gameState.passCheckpoint(v.getUuid(), cv.getCheckpointUUID());
                    //TODO: gameLog event for checkpoints
                }
            });

            //Inserting data about the airplane's movements to database
            gameState.getLog().insertEvent(
                    gameState.getGameCount(), Event.eventType.MOVEMENT.toString().toUpperCase(),
                    gameState.getTickCount(), v.getOwner(),0,
                    gameState.searchPlayerLogin(v.getOwner()),
                    v.getPosX(), v.getPosY(), v.getSpeed(), v.getHeading(),
                    v.getAltitude(), v.getUuid(), gameState.findAirplanesByPlayer(v.getOwner()));
        });

        TCAS.calculateCollisions(airplanes);

//            gameState.getLog().commit();
        gameState.setNewAirplanesOutput();
        System.out.println("1s has passed");
        gameState.simulationResume();
    }
}
