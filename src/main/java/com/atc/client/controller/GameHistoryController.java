package com.atc.client.controller;
import com.atc.client.model.GameHistory;
import com.atc.client.model.HistoryStream;
import com.atc.server.model.Event;
import com.atc.server.model.StreamReader;
import com.atc.server.Message;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class GameHistoryController  extends GenericController{
    @FXML Button stopButton;
    @FXML Button playButton;
    @FXML Button newGameButton;
    @FXML Button sendButton;
    @FXML TextField gameIdTextField;
    @FXML ScrollPane eventsScrollPane;
    @FXML VBox eventsHistory;
    @FXML Slider mySlider;
    @FXML VBox commandHistory;
    @FXML ScrollPane commandScrollPane;
    @FXML Button settingsButton;
    @FXML Button mainMenuButton;
    GameHistory gameHistory;
    HistoryStream stream;

    private void handleDataTransaction(int gameId){
        stream = new HistoryStream(gameSettings.getIpAddress());
        stream.setSearchedGameId(gameId);
        stream.run();
        gameHistory.setEvents(stream.getEvents());
        gameHistory.setCallsigns(stream.getCallsigns());
        gameHistory.setLogins(stream.getLogins());
    }

    @FXML
    public void initialize(){
        sendButton.setOnAction(e->{
            String possibleInteger = gameIdTextField.getText();
            try{
             int idGame = Integer.parseInt(possibleInteger);
             gameHistory = new GameHistory();
             gameHistory.setCurrentGameId(idGame);
             handleDataTransaction(idGame);
             gameHistory.populateVBoxes(eventsHistory, commandHistory);

            }catch (NumberFormatException ex){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("WARNING");
                alert.setHeaderText("idGame not a number");
                alert.setContentText("Please insert a number!");
                alert.showAndWait();
            }
        });
        playButton.setOnAction(e->{
        });
        newGameButton.setOnAction(e ->
            windowController.loadAndSetScene("/fxml/GameActivity.fxml", gameSettings));
        mainMenuButton.setOnAction(e ->
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings));
        settingsButton.setOnAction(e ->
            windowController.loadAndSetScene("/fxml/GameSettings.fxml", gameSettings));
    }
}
