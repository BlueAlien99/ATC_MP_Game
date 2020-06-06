package com.atc.client.controller;

import com.atc.client.model.ClientStreamHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.io.IOException;
/**
 * Class controlling everything that happens in GameSettings window.
 */

public class GameSettingsController extends GenericController{

    @FXML TextField loginTextField;
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
            loginTextField.setText(gameSettings.getClientName());
            ipAddressTextField.setText(gameSettings.getIpAddress());
            airplanesComboBox.getSelectionModel().select(gameSettings.getPlaneNum() - 1);
        });

        applyButton.setOnAction(e->{
            gameSettings.setPlaneNum(airplanesComboBox.getValue());
            gameSettings.setIpAddress(ipAddressTextField.getText());
            try {
                ClientStreamHandler.getInstance().updateIP();
            } catch (IOException ex) {
                gameSettings.setIpAddress("localhost");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Invalid IP");
                alert.setHeaderText(null);
                alert.setContentText("Unable to change IP. Reverted to localhost.");
                alert.showAndWait();
            }
            gameSettings.setClientName(loginTextField.getText());
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
        });

    }
}
