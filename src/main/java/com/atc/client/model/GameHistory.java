package com.atc.client.model;

import com.atc.server.model.Event;

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

    public void setAvailableReplayGames(List<Integer> availableReplayGames) {
        this.availableReplayGames = availableReplayGames;
    }

    public void setCheckpoints(List<Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
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
