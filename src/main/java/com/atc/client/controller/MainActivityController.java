package com.atc.client.controller;

import com.atc.client.model.ClientStreamHandler;
import com.atc.server.ServerMain;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.Optional;

public class MainActivityController extends GenericController{


    private static class HistoryChoice extends Alert{
        public static ButtonType bRemote = new ButtonType("Remote");
        public static ButtonType bLocal = new ButtonType("Local");
        public static ButtonType bCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        public HistoryChoice() {
            super(AlertType.CONFIRMATION);
            this.setTitle("Choose history type");
            this.setHeaderText("Do you want to browse local or remote history?");
            this.setContentText("Make your selection.\nTo browse remote history please configure the IP address in Settings");
            this.getButtonTypes().setAll(bRemote, bLocal, bCancel);
        }
    }
    private static class MultiPlayerChoice extends Alert{
        public static ButtonType bHost = new ButtonType("Host");
        public static ButtonType bJoin = new ButtonType("Join");
        public static ButtonType bCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        public MultiPlayerChoice() {
            super(AlertType.CONFIRMATION);
            this.setTitle("Choose multi player game type");
            this.setHeaderText("Do you want to join or host a game?");
            this.setContentText("Make your selection.\nTo join a game please configure the IP address in Settings");
            this.getButtonTypes().setAll(bHost, bJoin, bCancel);
        }
    }
    @FXML private Button bestScoresButton;
    @FXML private Button singlePlayerGameButton;
    @FXML private Button multiPlayerGameButton;
    @FXML private Button gameHistoryButton;
    @FXML private Button settingsButton;
    @FXML private Button quitButton;

    @FXML
    public void initialize(){

        try {
            ClientStreamHandler.getInstance().setStreamState(ClientStreamHandler.StreamStates.STREAM_IDLE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        multiPlayerGameButton.setOnAction(e -> {
            Optional<ButtonType> result = new MultiPlayerChoice().showAndWait();
            ButtonType buttonType = result.get();
            if (MultiPlayerChoice.bJoin.equals(buttonType)) {
                if(gameSettings.getIpAddress() != null) {
                    System.out.println(gameSettings.getClientUUID().toString() + " MainActivityController multiplayer");
                    windowController.loadAndSetScene("/fxml/GameActivity.fxml", gameSettings);
                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ERROR!");
                    alert.setHeaderText("Not initialized IP address!");
                    alert.setContentText("Specify IP address of your server.");
                    alert.showAndWait();
                }
            } else if (MultiPlayerChoice.bHost.equals(buttonType)) {
                gameSettings.setIpAddress("localhost");
                try {
                    ClientStreamHandler.getInstance().updateIP();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                windowController.loadAndSetScene("/fxml/GameActivity.fxml", gameSettings);
            }
        });

        gameHistoryButton.setOnAction(e ->{
            Optional<ButtonType> result = new HistoryChoice().showAndWait();
            ButtonType buttonType = result.get();
            if (HistoryChoice.bRemote.equals(buttonType)) {
                windowController.loadAndSetScene("/fxml/GameHistory.fxml", gameSettings);
            } else if (HistoryChoice.bLocal.equals(buttonType)) {
                gameSettings.setIpAddress("localhost");
                try {
                    ClientStreamHandler.getInstance().updateIP();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                windowController.loadAndSetScene("/fxml/GameHistory.fxml", gameSettings);
            }
        });
        bestScoresButton.setOnAction(e->
                windowController.loadAndSetScene("/fxml/BestScores.fxml", gameSettings));

        singlePlayerGameButton.setOnAction(e->
            windowController.loadAndSetScene("/fxml/GameCreator.fxml", gameSettings));
        settingsButton.setOnAction(e ->
                windowController.loadAndSetScene("/fxml/GameSettings.fxml", gameSettings));
        quitButton.setOnAction(e -> {
            ServerMain.getInstance().interrupt();
            ClientStreamHandler.getInstance().interrupt();
            Platform.exit();
        });
    }
}
