package com.atc.client.model;

import com.atc.server.model.Event;
import com.atc.server.model.Login;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class is basically a container for all the information needed in recreating gameplays.
 */
public class GameHistory {
    int currentGameId;
    List<Checkpoint> checkpoints;
    List<Event> Events;
    List<Integer> availableReplayGames;
    HashMap<UUID, String> Callsigns;
    HashMap<Integer, String> Logins;

    /**
     * Sets available replay games.
     *
     * @param availableReplayGames the available replay games
     */
    public void setAvailableReplayGames(List<Integer> availableReplayGames) {
        this.availableReplayGames = availableReplayGames;
    }

    /**
     * Sets checkpoints.
     *
     * @param checkpoints the checkpoints
     */
    public void setCheckpoints(List<Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    /**
     * Gets checkpoints.
     *
     * @return the checkpoints
     */
    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public List<Event> getEvents() {
        return Events;
    }

    /**
     * Sets events.
     *
     * @param events the events
     */
    public void setEvents(List<Event> events) {
        Events = events;
    }

    /**
     * Gets current game id.
     *
     * @return the current game id
     */
    public int getCurrentGameId() {
        return currentGameId;
    }

    /**
     * Sets current game id.
     *
     * @param currentGameId the current game id
     */
    public void setCurrentGameId(int currentGameId) {
        this.currentGameId = currentGameId;
    }

    /**
     * Sets callsigns.
     *
     * @param callsigns the callsigns
     */
    public void setCallsigns(HashMap<UUID, String> callsigns) {
        Callsigns = callsigns;
    }

    /**
     * Sets logins.
     *
     * @param logins the logins
     */
    public void setLogins(HashMap<Integer, String> logins) {
        Logins = logins;
    }

    /**
     * Gets available replay games.
     *
     * @return the available replay games
     */
    public List<Integer> getAvailableReplayGames() {
        return availableReplayGames;
    }

    /**
     * Gets callsigns.
     *
     * @return the callsigns
     */
    public HashMap<UUID, String> getCallsigns() {
        return Callsigns;
    }

    public HashMap<Integer, String> getLogins() {
        return Logins;
    }
}
