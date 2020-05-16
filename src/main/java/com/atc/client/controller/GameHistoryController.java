package com.atc.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

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

    @FXML
    public void initialize(){

        newGameButton.setOnAction(e->{
            windowController.loadAndSetScene("/fxml/GameActivity.fxml",gameSettings);
        });
        mainMenuButton.setOnAction(e ->{
            windowController.loadAndSetScene("/fxml/MainActivity.fxml",gameSettings);
        });

        settingsButton.setOnAction(e->{
            windowController.loadAndSetScene("/fxml/GameSettings.fxml",gameSettings);
        });
            }

}
