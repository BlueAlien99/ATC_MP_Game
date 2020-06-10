package com.atc.server.thread;

import com.atc.client.model.Airplane;
import com.atc.client.model.Checkpoint;
import com.atc.server.model.GameState;
import com.atc.server.utils.TCAS;
import com.atc.server.dao.model.Event;

import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.atc.client.GlobalConsts.*;

/**
 * Class with simulation thread which invokes all methods related to plane movement,
 * collision detection and checkpoint passing. One run of run() methods equals one tick of a simulation.
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

        if(gameState.getTickCount() % AI_GENERATION_FREQUENCY == 0 && gameState.getAiPlanes() < MAX_AI_PLANES){
            //System.out.println(gameState.getAiPlanes());
            gameState.generateAiPlane();
        }

        airplanes.forEach((k, v) -> {
            if(v.getPosX() > CANVAS_WIDTH * 3 / 2 || v.getPosX() < -CANVAS_WIDTH / 2 ||
                    v.getPosY() > CANVAS_HEIGHT * 3 / 2 || v.getPosY() < -CANVAS_HEIGHT / 2){
                if(v.getOwner() != null && !checkIfPassedAllCheckpoints(v)){
                    v.setPosY(CANVAS_WIDTH/2);
                    v.setPosX(CANVAS_HEIGHT/2);
                    gameState.getLog().insertEvent(gameState.getGameCount(), Event.eventType.OFFTHEBOARD.toString(),
                            gameState.getTickCount(),v.getOwner(), 0, gameState.getPlayersLogins().get(v.getOwner()),
                            v.getPosX(), v.getPosY(), v.getSpeed(), v.getHeading(),v.getAltitude(), v.getUuid(),
                            gameState.findAirplanesByPlayer(v.getOwner()));
                } else if(v.getOwner() != null && checkIfPassedAllCheckpoints(v)){
                    airplanes.remove(k);
                }else{
                    gameState.removeAiPlane(k);
                }
                return;
            }

            if(v.isCrashed()){
                if(v.getOwner() != null){
                    gameState.getLog().insertEvent(gameState.getGameCount(), Event.eventType.COLLISION.toString(), gameState.getTickCount(),
                            v.getOwner(), COLLISION_PENALTY ,gameState.getPlayersLogins().get(v.getOwner()), v.getPosX(), v.getPosY(),
                            v.getSpeed(), v.getHeading(), v.getAltitude(), v.getUuid(), gameState.findAirplanesByPlayer(v.getOwner()));
                    gameState.generateNewAirplanes(1, v.getOwner());
                    airplanes.remove(k);
                } else {
                    gameState.removeAiPlane(k);
                }
                return;
            }

            v.moveAirplane();

            //This is just for testing, so that user does not loose airplanes
            try {
                if(DEBUGGING_MODE) {
                    if (v.getPosX() < 0) v.setPosX(CANVAS_WIDTH);
                    if (v.getPosY() < 0) v.setPosY(CANVAS_HEIGHT);
                    if (v.getPosX() > CANVAS_WIDTH) v.setPosX(0);
                    if (v.getPosY() > CANVAS_HEIGHT) v.setPosY(0);
                    //used for collision detection, remove when 4 ifs above are removed
                    v.calculateABParams();
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }

            checkpoints.forEach((ck, cv) -> {
                if(cv.checkAirplane(v)){
                    //System.out.println(v.getUuid()+" JUST PASSED CHECKPOINT "+cv.getCheckpointUUID());
                    gameState.passCheckpoint(v.getUuid(), cv.getCheckpointUUID());
                }
            });

            //Inserting data about the airplane's movements to database
            gameState.getLog().insertEvent(
                    gameState.getGameCount(), Event.eventType.MOVEMENT.toString().toUpperCase(),
                    gameState.getTickCount(), v.getOwner(),0,
                    v.getOwner() == null ? "AI" : gameState.searchPlayerLogin(v.getOwner()),
                    v.getPosX(), v.getPosY(), v.getSpeed(), v.getHeading(),
                    v.getAltitude(), v.getUuid(), gameState.findAirplanesByPlayer(v.getOwner()));
        });

        TCAS.calculateCollisions(airplanes);

//            gameState.getLog().commit();
        gameState.setNewAirplanesOutput();
        //System.out.println("1s has passed");
        gameState.simulationResume();
    }

    /**
     * Checks if airplane passed all checkpoints - this method prevents spawning a new airplane
     * for player if
     * @param airplane
     * @return
     */
    private boolean checkIfPassedAllCheckpoints(Airplane airplane){
        for(Checkpoint checkpoint: checkpoints.values()){
            if(!checkpoint.getAirplanes().get(airplane.getUuid()))
                return false;
        }
        return true;
    }
}
