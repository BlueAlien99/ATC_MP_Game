package com.atc.client.controller;
import com.atc.client.model.Airplane;
import com.atc.client.model.GameCanvas;
import com.atc.client.model.GameHistory;
import com.atc.client.model.HistoryStream;
import com.atc.server.model.Event;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

public class GameHistoryController  extends GenericController {
    @FXML private HBox centerTop;
    @FXML private HBox centerBottom;
    @FXML private BorderPane centerPane;
    @FXML private GameCanvas radar;
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

    private int activeTimeTick =0;
    HashMap<UUID, Airplane> airplaneVector = new HashMap<>();
    GameHistory gameHistory;
    Semaphore threadSemaphore = new Semaphore(1);
    HistoryStream stream;
    Thread streamThread;
    airplaneTimerTask task;
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

    class airplaneTimerTask extends TimerTask{
        List<Event> events;
        int maxTimeTick;
        int actualTimeTick;
        Thread T;
        Timer time;
        public airplaneTimerTask(int maxTimeTick, List<Event> events){
            this.events = events;
            this.maxTimeTick=maxTimeTick;
        }
        void init(){
            time = new Timer();
            time.schedule(this, 0, 1000);
        }
        public void stop(){
            System.out.println("KONIEC");
            if(task != null){
                time.cancel();
                T.interrupt();
            }
            threadSemaphore.release();
        }
        @Override
        public void run(){
            System.out.println("W SORDKU TIMERA");
            System.out.println("ACTIVE " + activeTimeTick);
            System.out.println("ACTUAL " + actualTimeTick);
            System.out.println("MAX TIME TICK " + maxTimeTick);
            actualTimeTick = activeTimeTick;
            if (actualTimeTick == maxTimeTick) {
                stop();
            }
            T = new Thread(() -> {
                mySlider.setValue(activeTimeTick);
                activeTimeTick += 1;
                System.out.println("SRAKA");
            });
            T.start();
            actualTimeTick +=1;
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
            populateAirplaneHashmap(stream.getEvents());
        }
    }

    @FXML
    public void initialize() {
        eventsList.setCellFactory(studentListView -> new EventCell());
        commandsList.setCellFactory(studentListView -> new EventCell());
        Predicate<ComboBox> isComboBoxEmpty = gameIdComboBox -> gameIdComboBox.getItems().isEmpty();
        sendButton.setOnAction(e -> {
            if(task != null){
                task.stop();
            }
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
                mySlider.setMax(getMaxOfTicks(gameHistory.getEvents()));
                mySlider.setMin(getMinOfTicks(gameHistory.getEvents()));
                mySlider.setValue(getMinOfTicks(gameHistory.getEvents()));
                mySlider.setVisible(true);
                playButton.setVisible(true);
                stopButton.setVisible(true);
            }
        });

        eventsList.getSelectionModel().selectedItemProperty().addListener(e-> {
            if(task != null){
                task.stop();
            }
            if(eventsList.getSelectionModel().getSelectedItem() != null)
                drawAirplanes((Event) eventsList.getSelectionModel().getSelectedItem());
        });

        mySlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                   activeTimeTick = newValue.intValue();
                   populateAirplaneHashmap(gameHistory.getEvents());
                    radar.start_printing();
                    airplaneVector.forEach((key, value) -> radar.print_airplane(value));
                    Platform.runLater(()->radar.finish_printing());
                });

        playButton.setOnMouseClicked(e -> {
            if(threadSemaphore.tryAcquire()){
                List<Event> events = gameHistory.getEvents();
//                activeTimeTick = getMinOfTicks(events);
                int maxTimeTick = getMaxOfTicks(events);
                System.out.println("PRZED ZAINICJOWANIEM TIMERA");
                task = new airplaneTimerTask(maxTimeTick, events);
                task.init();
                stopButton.setOnAction(eventHandler-> task.stop());
               mySlider.setOnMouseClicked(event -> {
                   System.out.println("Zatrzymać SLIDER");
                   task.stop();
               });
            }

        });
        //TODO: stream throws null pointer exception, so the buttons doesnt work at all
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
        eventsList.getSelectionModel().getSelectedItem();
        Platform.runLater(()->{
            alignRadar();
            radar.start_printing();
            radar.finish_printing();
        });

    }

    private int getMaxOfTicks(List<Event> events){
        return events.get(events.size()-1).getTimeTick();
    }

    private int getMinOfTicks(List<Event> events){
        return events.get(0).getTimeTick();
    }


    private void populateAirplaneHashmap(List<Event> events){
        airplaneVector.clear();
        for(Event e : events){
            if(e.getTimeTick() == activeTimeTick){
                airplaneVector.put(e.getAirplaneUUID(),new Airplane(e.getAirplaneUUID(),
                        gameHistory.getCallsigns().get(e.getAirplaneUUID()),
                        gameHistory.getCallsigns().get(e.getAirplaneUUID()),
                        e.getxCoordinate(), e.getyCoordinate(),
                        e.getHeight(), e.getHeading(), e.getSpeed()));
            } else if (e.getTimeTick()> activeTimeTick){
                break;
            }
        }
    }

    private void chooseAirplanes(Event event){
        int newTimeTick = event.getTimeTick();
        if(activeTimeTick != newTimeTick){
            activeTimeTick = newTimeTick;
            mySlider.setValue(newTimeTick);
            populateAirplaneHashmap(gameHistory.getEvents());
        }
    }

    private void drawAirplanes(Event event){
        chooseAirplanes(event);
        radar.start_printing();
        airplaneVector.forEach((key, value) -> radar.print_airplane(value, value.getUuid() == event.getAirplaneUUID()));
        radar.finish_printing();
    }

    private void populateComboBox(ComboBox gameIdComboBox) {
        List<Integer> availableReplayGames = gameHistory.getAvailableReplayGames();
        gameIdComboBox.setItems(FXCollections.observableArrayList(availableReplayGames));
    }

    private void populateLists() {
        eventsList.getSelectionModel().clearSelection();
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
        commandString.append("(").append(event.getHeading());
        commandString.append(", ").append(event.getSpeed());
        commandString.append(", ").append(event.getHeight()).append(")");
        return commandString.toString();
    }

    private String createEventString(Event event, HashMap<UUID, String> Callsigns){
        StringBuilder eventString = new StringBuilder();
        eventString.append(event.getTimeTick());
        eventString.append(": ");
        eventString.append(Callsigns.get(event.getAirplaneUUID()));
        eventString.append("→ (");
        eventString.append(Math.round(event.getxCoordinate()));
        eventString.append(",").append(Math.round(event.getyCoordinate()));
        eventString.append(")");
        return eventString.toString();
    }

    void alignRadar(){
        int radarDimensions = Math.min((int)centerPane.getHeight()-(int)centerTop.getHeight()-(int)centerBottom.getHeight(), (int)centerPane.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);
    }

}
