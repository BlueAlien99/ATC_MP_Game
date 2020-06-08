package com.atc.client.controller;

import com.atc.client.model.*;
import com.atc.server.model.Event;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

/**
 * Class managing all the events happening in GameHistory window.
 */
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
    @FXML Button sendButton;
    @FXML Slider mySlider;
    @FXML Button settingsButton;
    @FXML Button mainMenuButton;

    private int activeTimeTick =0;
    HashMap<UUID, Airplane> airplaneVector = new HashMap<>();
    GameHistory gameHistory;
    Semaphore threadSemaphore = new Semaphore(1);
    airplaneTimerTask task;

    final int SENDGAMESID = -1;

    /**
     * Class representing a cell in TableView that contains information about events.
     */
    public class EventCell extends ListCell<Event>{
        private Label eventLabel=new Label();
        private int timeTick;

        /**
         * Method invoked for each element in TableView - it creates a label displayed for user.
         * @param event information included in this cell
         * @param empty variable that indicates if this cell is empty
         */
        @Override
        protected void updateItem(Event event , boolean empty) {
            super.updateItem(event, empty);

            if(empty || event == null) {

                setText(null);
                setGraphic(null);

            } else {
                if(event.getType() == Event.eventType.COMMAND){
                    eventLabel.setText(createCommandString(gameHistory.getLogins(), gameHistory.getCallsigns(), event));
                    setTextFill(Color.LIGHTBLUE);
                } else if(event.getType() == Event.eventType.MOVEMENT){
                    eventLabel.setText(createEventString(event, gameHistory.getCallsigns()));
                    setTextFill(Color.WHITE);
                } else if(event.getType() == Event.eventType.CHECKPOINT){
                    eventLabel.setText(createCheckpointString(event, gameHistory.getLogins(),
                            gameHistory.getCallsigns()));
                    setTextFill(Color.GHOSTWHITE);
                }else if(event.getType() == Event.eventType.COLLISION){
                    eventLabel.setText(createCollisionString(event, gameHistory.getLogins(),
                            gameHistory.getCallsigns()));
                    setTextFill(Color.RED);
                }
                timeTick = event.getTimeTick();
                setText(eventLabel.getText());
            }
        }
    }


    /**
     * Timer simulating ticks on server - it helps to synchronize displaying movement of airplanes every second.
     */
    class airplaneTimerTask extends TimerTask{

        List<Event> events;
        int maxTimeTick;
        int actualTimeTick;
        Thread T;
        Timer time;

        /**
         * Instantiates a new Airplane timer task.
         *
         * @param maxTimeTick the max time tick
         * @param events      the events
         */
        public airplaneTimerTask(int maxTimeTick, List<Event> events){
            this.events = events;
            this.maxTimeTick=maxTimeTick;
        }

        /**
         * Creates and initializes timer.
         */
        void init(){
            time = new Timer();
            time.schedule(this, 0, 1000);
        }

        /**
         * Stops the timer.
         */
        public void stop(){
            //System.out.println("KONIEC");
            if(task != null){
                time.cancel();
                T.interrupt();
            }
            threadSemaphore.release();
        }

        /**
         * After each second timer increments counter, until it reaches maximum value determined by a maximum id of the tick
         * of the replay from database
         */
        @Override
        public void run(){
            //System.out.println("W SORDKU TIMERA");
            //System.out.println("ACTIVE " + activeTimeTick);
            //System.out.println("ACTUAL " + actualTimeTick);
            //System.out.println("MAX TIME TICK " + maxTimeTick);
            actualTimeTick = activeTimeTick;
            if (actualTimeTick == maxTimeTick) {
                stop();
            }
                mySlider.setValue(activeTimeTick);
                activeTimeTick += 1;
            actualTimeTick +=1;
        }
    }

    /**
     * Method that manages exchange of messages between client and server.
     * Values below zero indicates that client only want a list of available replays.
     * Other values are used for requesting events from replays with corresponding ids.
     * @param gameId ID of the game that user want to replay
     */
    private void handleDataTransaction(int gameId) {
        ClientStreamHandler.getInstance().setSearchedGameId(gameId);
        try {
            ClientStreamHandler.getInstance().sendRequestForData();
            if (gameId < 0) {
                gameHistory.setAvailableReplayGames(ClientStreamHandler.getInstance().getAvailableGames());
            } else {
                gameHistory.setEvents(ClientStreamHandler.getInstance().getEvents());
                gameHistory.setCallsigns(ClientStreamHandler.getInstance().getCallsigns());
                gameHistory.setLogins(ClientStreamHandler.getInstance().getLogins());
                gameHistory.setCheckpoints(ClientStreamHandler.getInstance().getCheckpoints());
                populateAirplaneHashmap(ClientStreamHandler.getInstance().getEvents());
            }
        }catch (IOException| InterruptedException | NullPointerException ex){
            createAlert("Server message",
                    "Cannot connect to server. Please check IP address in your settings");
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
                //System.out.println("COMBO BOX SHOULD BE EMPTY");
                gameHistory = new GameHistory();
                try {
                    ClientStreamHandler.getInstance().setStreamState(ClientStreamHandler.StreamStates.STREAM_HISTORY);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
                handleDataTransaction(SENDGAMESID);
                populateComboBox(gameIdComboBox);
                if (!isComboBoxEmpty.test(gameIdComboBox)) {
                    gameIdComboBox.setVisible(true);
                    sendButton.setText("Show!");
                } else {
                    createAlert("Database message","No available replays");
                }
            } else {
                //System.out.println("COMBO BOX SHOULD BE FULL");
                try{
                    int idGame = (int) gameIdComboBox.getValue();
                    //System.out.println(idGame);
                    gameHistory.setCurrentGameId(idGame);
                    handleDataTransaction(idGame);
                    populateLists();
                    mySlider.setMax(getMaxOfTicks(gameHistory.getEvents()));
                    mySlider.setMin(getMinOfTicks(gameHistory.getEvents()));
                    mySlider.setValue(getMinOfTicks(gameHistory.getEvents()));
                    radar.start_printing();
                    printCheckpoints();
                    mySlider.setVisible(true);
                    playButton.setVisible(true);
                    stopButton.setVisible(true);
                } catch(NullPointerException ex){
                    createAlert("ComboBox issue",
                            "Please select an option.");
                }
            }

        });
        commandsList.getSelectionModel().selectedItemProperty().addListener(e->{
            if(task != null){
                task.stop();
            }
            if(commandsList.getSelectionModel().getSelectedItem() != null){
                int gameTimeTick = ((Event) commandsList.getSelectionModel().getSelectedItem()).getTimeTick();
                UUID activeUUID = ((Event) commandsList.getSelectionModel().getSelectedItem()).getAirplaneUUID();
                activeTimeTick = gameTimeTick;
                mySlider.setValue(activeTimeTick);
                radar.print_airplane(airplaneVector.get(activeUUID), true);
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
                    radar.removeAirplanes();
                    airplaneVector.forEach((key, value) -> radar.print_airplane(value));
                    Platform.runLater(()->radar.finish_printing());
                });

        playButton.setOnMouseClicked(e -> {
            if(threadSemaphore.tryAcquire()){
                List<Event> events = gameHistory.getEvents();
//                activeTimeTick = getMinOfTicks(events);
                int maxTimeTick = getMaxOfTicks(events);
                //System.out.println("PRZED ZAINICJOWANIEM TIMERA");
                task = new airplaneTimerTask(maxTimeTick, events);
                task.init();
                stopButton.setOnAction(eventHandler-> task.stop());
                mySlider.setOnMouseClicked(event -> {
                    //System.out.println("Zatrzymać SLIDER");
                    task.stop();
                });
            }

        });
        mainMenuButton.setOnAction(e -> {
            if(task != null){
                task.stop();
            }
            if(ClientStreamHandler.getInstance() != null) {
                try {
                    ClientStreamHandler.getInstance().setStreamState(ClientStreamHandler.StreamStates.STREAM_IDLE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
        });
        settingsButton.setOnAction(e -> {
            if(task != null){
                task.stop();
            }
            if(ClientStreamHandler.getInstance() != null) {
                try {
                    ClientStreamHandler.getInstance().sayGoodbye();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            windowController.loadAndSetScene("/fxml/GameSettings.fxml", gameSettings);
        });
        eventsList.getSelectionModel().getSelectedItem();
        Platform.runLater(()->{
            alignRadar();
            radar.removeAirplanes();
            radar.finish_printing();
        });

    }

    /**
     * Simple method to create a new widow to alert user that something went wrong
     * @param header header of the alert - where this happened?
     * @param message the message itself - what happened?
     */
    private void createAlert(String header, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Searches for last tick in game on events list
     * @param events list of events in game
     * @return maximum value of event_id
     */
    private int getMaxOfTicks(List<Event> events){
        return events.get(events.size()-1).getTimeTick();
    }

    /**
     * Searches for first tick in game on events list
     * @param events list of events in game
     * @return minimum value of event_id
     */
    private int getMinOfTicks(List<Event> events){
        return events.get(0).getTimeTick();
    }

    /**
     * Creates and put in hashmap airplanes connected to time tick defined in activeTimeTick variable
     * @param events list of events in game
     */
    private void populateAirplaneHashmap(List<Event> events){
        airplaneVector.clear();
        for(Event e : events){
            if(e.getTimeTick() == activeTimeTick){
                airplaneVector.put(e.getAirplaneUUID(),new Airplane(e.getAirplaneUUID(),
                        gameHistory.getCallsigns().get(e.getAirplaneUUID()), "",
                        e.getxCoordinate(), e.getyCoordinate(),e.getHeight(),
                        e.getHeading(),e.getSpeed()));
            } else if (e.getTimeTick()> activeTimeTick){
                break;
            }
        }
    }

    /**
     * Sets new activeTimeTick, changes position on a slider and populates hashmap with airplanes from a given time tick.
     * @param event event
     */
    private void chooseAirplanes(Event event){
        int newTimeTick = event.getTimeTick();
        if(activeTimeTick != newTimeTick){
            activeTimeTick = newTimeTick;
            mySlider.setValue(newTimeTick);
            populateAirplaneHashmap(gameHistory.getEvents());
        }
    }

    /**
     * Draw airplanes on the GameCanvas.
     * @param event event that determines tick we display on GameCanvas
     */
    private void drawAirplanes(Event event){
        chooseAirplanes(event);
        radar.removeAirplanes();
        airplaneVector.forEach((key, value) -> radar.print_airplane(value, value.getUuid() == event.getAirplaneUUID()));
        radar.finish_printing();
    }

    /**
     * Fills Combobox with ids of available replays.
     * @param gameIdComboBox - ComboBox we want to fill
     */
    private void populateComboBox(ComboBox gameIdComboBox) {
        List<Integer> availableReplayGames = gameHistory.getAvailableReplayGames();
        if(availableReplayGames != null )
            gameIdComboBox.setItems(FXCollections.observableArrayList(availableReplayGames));
    }

    /**
     * Depending on type of event it creates different cells and fills each of the commandListView and eventsListView with data.
     * commandsListView - commands from player.
     * eventsListView - all the types of events - checkpoints, movements etc.
     */
    private void populateLists() {
        eventsList.getSelectionModel().clearSelection();
        eventsList.getItems().clear();
        commandsList.getItems().clear();
        List<Event> Events = gameHistory.getEvents();
        ObservableList<Event> eventsObservableList = FXCollections.observableArrayList();
        ObservableList<Event> commandsObservableList = FXCollections.observableArrayList();

        for (Event event : Events) {
            if (event.getType() == Event.eventType.MOVEMENT || event.getType()== Event.eventType.CHECKPOINT
            || event.getType() == Event.eventType.COLLISION) {
                eventsObservableList.add(event);
            } else {
                commandsObservableList.add(event);
            }
        }
        eventsList.setItems(eventsObservableList);
        commandsList.setItems(commandsObservableList);
    }

    /**
     * Simple method to create a label for a command event
     * @param Logins - logins of players
     * @param Callsigns - callsigns of airplanes
     * @param event - commmand event, containing all the necessary data
     * @return command label
     */
    private String createCommandString(HashMap<Integer, String> Logins, HashMap<UUID, String> Callsigns, Event event) {
        return event.getTimeTick() +
                ": " +
                Logins.get(event.getPlayerId()) +
                " → " +
                Callsigns.get(event.getAirplaneUUID()) +
                "(" + event.getHeading() +
                ", " + event.getSpeed() +
                ", " + event.getHeight() + ")";
    }

    /**
     * Simple method to create a label for movement event
     * @param event - event containing necessary data
     * @param Callsigns - callsigns of airplanes
     * @return event label
     */
    private String createEventString(Event event, HashMap<UUID, String> Callsigns){
        return event.getTimeTick() +
                ": " +
                Callsigns.get(event.getAirplaneUUID()) +
                "→ (" +
                Math.round(event.getxCoordinate()) +
                "," + Math.round(event.getyCoordinate()) +
                ")";
    }

    /**
     *Simple method to create a label for collision event
     * @param event - event
     * @param logins - logins
     * @param callsigns - callsigns
     * @return collision string
     */
    private String createCollisionString(Event event, HashMap<Integer, String> logins, HashMap<UUID, String> callsigns) {
    return "COLLISION! " + "(" + logins.get(event.getPlayerId()) +")"
            + callsigns.get(event.getAirplaneUUID());

    }
    /**
     * Simple method to create a label for passing checkpoint event
     * @param event event
     * @param Logins logins
     * @param Callsigns callsings
     * @return checkpoint string
     */
    private String createCheckpointString(Event event, HashMap<Integer, String> Logins,
                                          HashMap<UUID, String> Callsigns ){
        return event.getTimeTick() +
                ": " + Logins.get(event.getPlayerId())+
                "("+Callsigns.get(event.getAirplaneUUID())+")"
                + " gains " + event.getPoints() + " points!";
    }

    /**
     * Align radar to new dimensions of the window.
     */
    void alignRadar(){
        int radarDimensions = Math.min((int)centerPane.getHeight()-(int)centerTop.getHeight()-(int)centerBottom.getHeight(), (int)centerPane.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);
    }

    /**
     * Print checkpoints on GameCanvas.
     */
    void printCheckpoints(){
        for(Checkpoint checkpoint: gameHistory.getCheckpoints()){
            radar.printCheckpoint(checkpoint);
        }
    }
}
