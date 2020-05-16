package com.atc.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class MainActivityController extends GenericController{

    @FXML private Button singlePlayerGameButton;
    @FXML private Button multiPlayerGameButton;
    @FXML private Button gameHistoryButton;
    @FXML private Button settingsButton;
    @FXML private Button quitButton;

    @FXML
    public void initialize(){

        multiPlayerGameButton.setOnAction(e -> {
            if(gameSettings.getIpAddress() != null) {
                System.out.println(gameSettings.getClientUUID().toString() + " MAC multiplayer");
                windowController.loadAndSetScene("/fxml/GameActivity.fxml", gameSettings);
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR!");
                alert.setHeaderText("Not initialized IP address!");
                alert.setContentText("Specify IP address of your server.");
                alert.showAndWait();
            }
        });

        gameHistoryButton.setOnAction(e ->
            windowController.loadAndSetScene("/fxml/GameHistory.fxml", gameSettings));
        settingsButton.setOnAction(e ->
                windowController.loadAndSetScene("/fxml/GameSettings.fxml", gameSettings));

        quitButton.setOnAction(e -> Platform.exit());
    }
}
