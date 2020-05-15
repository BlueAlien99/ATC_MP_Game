package com.atc.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;

public class GameHistoryController  extends GenericController{
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

    }

}
