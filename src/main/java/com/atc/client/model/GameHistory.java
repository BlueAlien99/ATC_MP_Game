package com.atc.client.model;

import com.atc.server.model.Event;
import com.atc.server.model.Login;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameHistory {
    int currentGameId;
    List<Event> Events;
    HashMap<UUID, String> Callsigns;
    HashMap<Integer, String> Logins;

    public void populateVBoxes(VBox eventsVBox, VBox commandVBox) {
        Logins.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });
        for (Event event : Events) {
            if (event.getType() != Event.eventType.COMMAND) {
                StringBuilder eventString = new StringBuilder();
                eventString.append(event.getTimeTick());
                eventString.append(": ");
                eventString.append(Callsigns.get(event.getAirplaneUUID()));
                eventString.append("→ (");
                eventString.append(Math.round(event.getxCoordinate()));
                eventString.append("," + Math.round(event.getyCoordinate()));
                eventString.append(")");
                Label msgLabel = new Label(eventString.toString());
                msgLabel.setFont(new Font("Comic Sans MS", 14));
                msgLabel.setTextFill(Color.GREEN);
                eventsVBox.getChildren().add(msgLabel);
            }else {
                StringBuilder commandString = new StringBuilder();
                commandString.append(event.getTimeTick());
                commandString.append(": ");
                commandString.append(Logins.get(event.getPlayerId()));
                commandString.append(" → ");
                commandString.append(Callsigns.get(event.getAirplaneUUID()));
                commandString.append("(" + event.getHeading());
                commandString.append(", "+event.getSpeed());
                commandString.append(", " +event.getHeight() + ")");
                Label msgLabel = new Label(commandString.toString());
                msgLabel.setFont(new Font("Comic Sans MS", 14));
                msgLabel.setTextFill(Color.GREEN);
                commandVBox.getChildren().add(msgLabel);

            }
        }

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
}
