package com.atc.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class GameSettingsController extends GenericController{

    @FXML TextField ipAddressTextField;
    @FXML ComboBox<Integer> airplanesComboBox;
    @FXML Button applyButton;
    @FXML Button mainMenuButton;
    private ObservableList<Integer> options =
            FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10);

    @FXML
    public void initialize(){
        airplanesComboBox.setItems(options);
        mainMenuButton.setOnAction(e ->
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings));

        Platform.runLater(() -> {
            ipAddressTextField.setText(gameSettings.getIpAddress());
            airplanesComboBox.getSelectionModel().select(gameSettings.getPlaneNum() - 1);
        });

        applyButton.setOnAction(e->{
            gameSettings.setPlaneNum(airplanesComboBox.getValue());
            gameSettings.setIpAddress(ipAddressTextField.getText());
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
        });

    }
}
