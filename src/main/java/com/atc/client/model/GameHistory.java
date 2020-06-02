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

public class GameHistory {
    int currentGameId;
    List<Checkpoint> checkpoints;
    List<Event> Events;
    List<Integer> availableReplayGames;
    HashMap<UUID, String> Callsigns;
    HashMap<Integer, String> Logins;
    HistoryStream stream=null;

    public void setAvailableReplayGames(List<Integer> availableReplayGames) {
        this.availableReplayGames = availableReplayGames;
    }

    public void setCheckpoints(List<Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public HistoryStream getStream() {
        return stream;
    }

    public void setStream(HistoryStream stream) {
        this.stream = stream;
    }

    public List<Event> getEvents() {
        return Events;
    }

    public void setEvents(List<Event> events) {
        Events = events;
    }

    public int getCurrentGameId() {
        return currentGameId;
    }

    public void setCurrentGameId(int currentGameId) {
        this.currentGameId = currentGameId;
    }

    public void setCallsigns(HashMap<UUID, String> callsigns) {
        Callsigns = callsigns;
    }

    public void setLogins(HashMap<Integer, String> logins) {
        Logins = logins;
    }

    public List<Integer> getAvailableReplayGames() {
        return availableReplayGames;
    }

    public HashMap<UUID, String> getCallsigns() {
        return Callsigns;
    }

    public HashMap<Integer, String> getLogins() {
        return Logins;
    }
}
