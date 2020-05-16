package com.atc.client.controller;

import com.atc.client.model.GameSettings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainActivityController extends GenericController{


    @FXML private Button singlePlayerGameButton;
    @FXML private Button multiPlayerGameButton;
    @FXML private Button gameHistoryButton;
    @FXML private Button settingsButton;
    @FXML private Button quitButton;

    @FXML
    public void initialize(){

        multiPlayerGameButton.setOnAction(e -> {
            if(gameSettings.getIpAddress() == null && gameSettings.getPlaneNum()!=0) {
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
        gameHistoryButton.setOnAction(e -> {
            windowController.loadAndSetScene("/fxml/GameHistory.fxml",gameSettings);
        });
        settingsButton.setOnAction(e->
                windowController.loadAndSetScene("/fxml/GameSettings.fxml",gameSettings));
        quitButton.setOnAction(e -> Platform.exit());
    }
}
