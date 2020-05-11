package com.atc.client.controller;

import com.atc.client.model.GameSettings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainActivityController extends GenericController{

    private GameSettings gs;

    @FXML
    private StackPane root;

    @FXML
    public void initialize(){
        gs = new GameSettings();
        Button newGameButton = new Button("New Game");
        Button quitButton = new Button("Quit");
        TextField ipAddress = new TextField("IP address");

        ObservableList<Integer> options =
                FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        ComboBox comboBox = new ComboBox(options);

        newGameButton.setOnAction(e -> {
            gs.setPlaneNum((Integer) comboBox.getValue());
            gs.setIpAddress(ipAddress.getText());
            windowController.loadAndSetScene("/fxml/GameActivity.fxml", gs);
        });

        quitButton.setOnAction(e -> Platform.exit());

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(newGameButton, quitButton, ipAddress, comboBox);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vbox);

        root.getChildren().add(borderPane);
    }
}
