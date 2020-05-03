package com.atc.client.controller;

import com.atc.client.Dimensions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainActivityController extends GenericController{

    @FXML
    private StackPane root;

    @FXML
    public void initialize(){
        Button newGameButton = new Button("New Game");
        Button quitButton = new Button("Quit");
        TextField ipAddress = new TextField("IP address");

        newGameButton.setOnAction(e -> {
            Dimensions.ipAddressDim = ipAddress.getText();
            windowController.loadAndSetScene("/fxml/GameActivity.fxml");
        });
        quitButton.setOnAction(e -> Platform.exit());

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(newGameButton, quitButton, ipAddress);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vbox);

        root.getChildren().add(borderPane);
    }
}
