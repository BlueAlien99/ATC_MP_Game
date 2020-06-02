package com.atc.server;

import com.atc.client.Dimensions;
import com.atc.client.model.Airplane;
import com.atc.client.model.Checkpoint;
import com.atc.client.model.GameSettings;
import com.atc.client.model.TCAS;
import com.atc.server.gamelog.GameLog;
import javafx.util.Pair;

import java.util.Random;
import java.util.Timer;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static com.atc.client.Dimensions.*;

public class GameState {

    private Timer simulationTimer;

    private ConcurrentHashMap<String, ClientConnection> connections = new ConcurrentHashMap<>();

    private ConcurrentHashMap<UUID, Airplane> airplanes = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Airplane> airplanesOutput = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String> chatMessages = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, String> playersLogins = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Checkpoint> checkpoints = new ConcurrentHashMap<>();
    private Vector<Pair<UUID, UUID>> checkpointsAirplanesMapping = new Vector<>();

    private GameLog log = new GameLog();

    private Semaphore gameRunning = new Semaphore(0, true);

    private int tickCount = 0;

    private boolean checkpointsUpdated = false;

    private final Object outputBufferLock = new Object();
    private final Object checkpointBufferLock = new Object();

    private GameSettings gs;
    private int gameCount = log.selectGameId();

    private boolean shutdown = false;

    private int currPlaying = 0;

    public void setShutdown(boolean val) {
        shutdown = val;
    }

    public boolean getShutdown() {
        return shutdown;
    }

    public void addConnection(String key, ClientConnection value) {
        if (connections.isEmpty()) {
            simulationTimer = new Timer(true);
            simulationTimer.scheduleAtFixedRate(new Simulation(this), SIM_TICK_DELAY, SIM_TICK_DELAY);
        }
        if (connections.get(key) == null) {
            simulationPause();
            connections.put(key, value);
        }
    }

    public boolean simulationPaused() {
        return gameRunning.tryAcquire();
    }

    public void simulationResume() {
        if (!gameRunning.tryAcquire())
            gameRunning.release();
    }

    public void simulationPause() {
        //pausing the game on player connection. This is the way to do it as not-a-toggle
        while (true) {
            if (!gameRunning.tryAcquire()) break;
        }
    }

    public void simulationPauseResume() {
        simulationResume();
    }

    public void removeConnection(String key) {
        connections.remove(key);
        if (connections.isEmpty()) {
            if (simulationTimer != null)
                simulationTimer.cancel(); /*TODO: it won't work, Rafał, as interrupts don't work on threads that have to do with ObjectStreams
                                            edit: maybe it will now, idk ~BJ*/
        }
    }

    public void generateNewAirplanes(int num, UUID owner) {
        int generated = 0;
        while(generated < num) {
            double pox = CANVAS_WIDTH / 8 + new Random().nextInt((int) CANVAS_WIDTH * 3 / 4);
            double poy = CANVAS_HEIGHT / 8 + new Random().nextInt((int) CANVAS_HEIGHT * 3 / 4);
            double alt = new Random().nextInt(8)*500 + 8000;
            double head = new Random().nextInt(360);
            double speed = new Random().nextInt((int) (DEFAULT_MAX_SPEED - DEFAULT_MIN_SPEED) / 2) + DEFAULT_MIN_SPEED;

            if(DEBUGGING_MODE) {
                alt = 5000;
                speed = 160;
            }

            Airplane airplane = new Airplane(owner, pox, poy, alt, head, speed);
            UUID newUUID = airplane.getUuid();

            ConcurrentHashMap<UUID, Airplane> checkForCollisions = new ConcurrentHashMap<>();
            airplanes.forEach((k, v ) -> {
                try {
                    checkForCollisions.put(k, (Airplane)v.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            });

            checkForCollisions.put(newUUID, airplane);
            TCAS.calculateSingleCollision(checkForCollisions, newUUID);

            if(checkForCollisions.get(newUUID).isCollisionCourse() || checkForCollisions.get(newUUID).isCrashed()){
                continue;
            }

            ++generated;
            airplanes.put(newUUID, airplane);
            log.insertCallsign(gameCount, airplane.getUuid(), airplane.getCallsign());
        }
    }

    public void updateAirplane(Airplane airplane, UUID clientUUID) {
        if (airplane == null || clientUUID == null) {
            return;
        }
        Airplane airplaneInGame = airplanes.get(airplane.getUuid());
        if (airplaneInGame == null || !clientUUID.equals(airplaneInGame.getOwner())) {
            return;
        }
        String chatMsg = ChatMsgParser.parseNewMsg(airplaneInGame, airplane);
        if (chatMsg == null) {
            return;
        }
        chatMessages.put(chatMessages.size(), chatMsg);
        airplaneInGame.setTargetParams(airplane.getTargetSpeed(), airplane.getTargetHeading(), airplane.getTargetAltitude());
        sendCommandToDatabase(airplane, clientUUID);
        synchronized (outputBufferLock) {
            outputBufferLock.notifyAll();
        }
    }

    private void sendCommandToDatabase(Airplane airplane, UUID clientUUID) {
        log.insertEvent(gameCount, "COMMAND", tickCount, clientUUID, 0,
                playersLogins.get(clientUUID), airplane.getPosX(), airplane.getPosY(),
                airplane.getTargetSpeed(), airplane.getTargetHeading(), airplane.getTargetAltitude(), airplane.getUuid());
//        log.commit();
    }


    public void setNewAirplanesOutput() {
        airplanesOutput = new ConcurrentHashMap<>();
        airplanes.forEach((k, v) -> {
            try {
                airplanesOutput.put(k, (Airplane) v.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        ++tickCount;
        synchronized (outputBufferLock) {
            outputBufferLock.notifyAll();
        }
    }

    public Object getOutputBufferLock() {
        return outputBufferLock;
    }

    public ConcurrentHashMap<UUID, Airplane> getAirplanes() {
        return airplanes;
    }

    public ConcurrentHashMap<UUID, Airplane> getAirplanesOutput() {
        return airplanesOutput;
    }

    public int getTickCount() {
        return tickCount;
    }

    public ConcurrentHashMap<Integer, String> getChatMessages() {
        return chatMessages;
    }

    public GameLog getLog() {
        return log;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void addPlayerLogin(UUID playerUUID, String playerLogin) {
        playersLogins.put(playerUUID, playerLogin);
    }

    public String searchPlayerLogin(UUID playerUUID) {
        return playersLogins.get(playerUUID);
    }

    public void sendMessageToAll(String s) {
        chatMessages.put(chatMessages.size(), s);
        synchronized (outputBufferLock) {
            outputBufferLock.notifyAll();
        }
    }

    public void incCurrPlaying() {
        currPlaying++;
    }

    public void decCurrPlaying() {
        currPlaying--;
        if (currPlaying == 0) {
            simulationPause();
        }
    }

    public void passCheckpoint(UUID airplaneUUID, UUID checkpointUUID){
        if(checkpoints.get(checkpointUUID)==null || airplanes.get(airplaneUUID)==null)
            return;

        checkpoints.get(checkpointUUID).passAirplane(airplaneUUID);

        setNewCheckpointsAirplanesMapping();

        String msg = ((playersLogins.get(airplanes.get(airplaneUUID).getOwner()) != null) ? playersLogins.get(airplanes.get(airplaneUUID).getOwner()) : "Unknown player")
                + "'s flight " + airplanes.get(airplaneUUID).getCallsign() + " passed checkpoint! They'll get "
                + checkpoints.get(checkpointUUID).getPoints() + " points!";
        checkpointsUpdated = true;
        sendMessageToAll(msg);
    }

    public void setNewCheckpointsAirplanesMapping(){
        checkpointsAirplanesMapping = new Vector<>();
        checkpoints.forEach(((uuid, checkpoint) -> {
            checkpoint.airplanes.forEach(((uuid1, aBoolean) -> {
                if(aBoolean)
                    checkpointsAirplanesMapping.add(new Pair<>(uuid, uuid1));
            }));
        }));
    }

    public ConcurrentHashMap<UUID, Checkpoint> getCheckpoints() {
        return checkpoints;
    }


    public void setCheckpoints(ConcurrentHashMap<UUID, Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }
    public void addCheckpoint(Checkpoint checkpoint) {
        airplanes.forEach(((uuid, airplane) -> checkpoint.addAirplane(uuid)));
        checkpoints.put(checkpoint.getCheckpointUUID(), checkpoint);
    }

    public void setAirplanes(ConcurrentHashMap<UUID, Airplane> airplanes) {
        this.airplanes = airplanes;
    }

    public void addAirplane(Airplane airplane){
        checkpoints.forEach(((uuid, checkpoint) -> checkpoint.addAirplane(airplane.getUuid())));
        airplanes.put(airplane.getUuid(), airplane);
    }

    public boolean getCheckpointsUpdated() {
        return checkpointsUpdated;
    }

    public void setCheckpointsUpdated(boolean checkpointsUpdated) {
        this.checkpointsUpdated = checkpointsUpdated;
    }

    public Vector<Pair<UUID, UUID>> getCheckpointsAirplanesMapping() {
        return checkpointsAirplanesMapping;
    }
}