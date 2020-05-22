package com.atc.client.controller;
import com.atc.client.model.GameHistory;
import com.atc.client.model.HistoryStream;
import com.atc.server.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.swing.tree.ExpandVetoException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class GameHistoryController  extends GenericController {
    @FXML private ComboBox gameIdComboBox;
    @FXML private ListView eventsList;
    @FXML private ListView commandsList;
    @FXML Button stopButton;
    @FXML Button playButton;
    @FXML Button newGameButton;
    @FXML Button sendButton;
    @FXML Slider mySlider;
    @FXML Button settingsButton;
    @FXML Button mainMenuButton;
    GameHistory gameHistory;
    HistoryStream stream;
    Thread streamThread;
    final int SENDGAMESID = -1;

    private class EventCell extends ListCell<Event>{
        private Label eventLabel=new Label();
        private int timeTick;

        @Override
        protected void updateItem(Event event , boolean empty) {
            super.updateItem(event, empty);

            if(empty || event == null) {

                setText(null);
                setGraphic(null);

            } else {
                if(event.getType() == Event.eventType.COMMAND){
                    eventLabel.setText(createCommandString(gameHistory.getLogins(), gameHistory.getCallsigns(), event));
                    setTextFill(Color.BLUEVIOLET);
                } else if(event.getType() == Event.eventType.MOVEMENT){
                    eventLabel.setText(createEventString(event, gameHistory.getCallsigns()));
                    setTextFill(Color.SPRINGGREEN);
                }
                timeTick = event.getTimeTick();
                setText(eventLabel.getText());
            }
        }

    }

    private void initializeStream() {
        stream = new HistoryStream(gameSettings.getIpAddress());
        gameHistory.setStream(stream);
        streamThread = new Thread(stream);
        streamThread.start();
    }

    private void handleDataTransaction(int gameId) {
        stream.setSearchedGameId(gameId);
        stream.sendRequestForData();
        if (gameId < 0) {
            gameHistory.setAvailableReplayGames(stream.getAvailableGames());
        } else {
            gameHistory.setEvents(stream.getEvents());
            gameHistory.setCallsigns(stream.getCallsigns());
            gameHistory.setLogins(stream.getLogins());
        }
    }

    @FXML
    public void initialize() {
        eventsList.setCellFactory(studentListView -> new EventCell());
        commandsList.setCellFactory(studentListView -> new EventCell());
        Predicate<ComboBox> isComboBoxEmpty = gameIdComboBox -> gameIdComboBox.getItems().isEmpty();
        sendButton.setOnAction(e -> {
            if (isComboBoxEmpty.test(gameIdComboBox)) {
                System.out.println("COMBO BOX SHOULD BE EMPTY");
                gameHistory = new GameHistory();
                initializeStream();
                handleDataTransaction(SENDGAMESID);
                populateComboBox(gameIdComboBox);
                if (!isComboBoxEmpty.test(gameIdComboBox)) {
                    gameIdComboBox.setVisible(true);
                    sendButton.setText("Show!");
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Database message");
                    alert.setHeaderText(null);
                    alert.setContentText("No available replays.");
                    alert.showAndWait();
                }
            } else {
                System.out.println("COMBO BOX SHOULD BE FULL");
                int idGame = (int) gameIdComboBox.getValue();
                gameHistory.setCurrentGameId(idGame);
                handleDataTransaction(idGame);
                populateLists();
                mySlider.setMax(gameHistory.getEvents().size());
                mySlider.setVisible(true);
                playButton.setVisible(true);
                stopButton.setVisible(true);
            }
        });
        playButton.setOnAction(e -> {
        });
        newGameButton.setOnAction(e -> {
            stream.sayGoodbye();
            windowController.loadAndSetScene("/fxml/GameActivity.fxml", gameSettings);
        });
        mainMenuButton.setOnAction(e -> {
            stream.sayGoodbye();
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
        });
        settingsButton.setOnAction(e -> {
            stream.sayGoodbye();
            windowController.loadAndSetScene("/fxml/GameSettings.fxml", gameSettings);
        });
    }

    private void populateComboBox(ComboBox gameIdComboBox) {
        List<Integer> availableReplayGames = gameHistory.getAvailableReplayGames();
        gameIdComboBox.setItems(FXCollections.observableArrayList(availableReplayGames));
    }

    private void populateLists() {
        eventsList.getItems().clear();
        commandsList.getItems().clear();
        List<Event> Events = gameHistory.getEvents();
        ObservableList<Event> eventsObservableList = FXCollections.observableArrayList();
        ObservableList<Event> commandsObservableList = FXCollections.observableArrayList();

        for (Event event : Events) {
            if (event.getType() == Event.eventType.MOVEMENT) {
                eventsObservableList.add(event);
            } else if(event.getType() == Event.eventType.COMMAND){
                commandsObservableList.add(event);
            }
        }
        eventsList.setItems(eventsObservableList);
        commandsList.setItems(commandsObservableList);
    }


    private String createCommandString(HashMap<Integer, String> Logins, HashMap<UUID, String> Callsigns, Event event) {
        StringBuilder commandString = new StringBuilder();
        commandString.append(event.getTimeTick());
        commandString.append(": ");
        commandString.append(Logins.get(event.getPlayerId()));
        commandString.append(" → ");
        commandString.append(Callsigns.get(event.getAirplaneUUID()));
        commandString.append("(" + event.getHeading());
        commandString.append(", " + event.getSpeed());
        commandString.append(", " + event.getHeight() + ")");
        return commandString.toString();
    }

    private String createEventString(Event event, HashMap<UUID, String> Callsigns){
        StringBuilder eventString = new StringBuilder();
        eventString.append(event.getTimeTick());
        eventString.append(": ");
        eventString.append(Callsigns.get(event.getAirplaneUUID()));
        eventString.append("→ (");
        eventString.append(Math.round(event.getxCoordinate()));
        eventString.append("," + Math.round(event.getyCoordinate()));
        eventString.append(")");
        return eventString.toString();
    }

}
