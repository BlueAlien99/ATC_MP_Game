package com.atc.client.controller;

import com.atc.client.model.GameSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class GameSettingsController extends GenericController{

    @FXML TextField ipAddress;
    @FXML ComboBox airplanesComboBox;
    @FXML Button applyButton;
    @FXML Button mainMenuButton;
    private ObservableList<Integer> options =
            FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10);
    @FXML
    public void initialize(){
        airplanesComboBox.setItems(options);
        mainMenuButton.setOnAction(e ->{
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
        });

        applyButton.setOnAction(e->{
            gameSettings.setPlaneNum((Integer) airplanesComboBox.getValue());
            gameSettings.setIpAddress(ipAddress.getText());
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
        });

    }
}
