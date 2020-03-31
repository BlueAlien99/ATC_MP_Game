package com.atc.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class GameActivityController extends GenericController {

    @FXML private Pane root;
    @FXML private GridPane centerGrid;
    @FXML private StackPane radar;
    @FXML private Rectangle rectRadarBg;
    @FXML private Pane chatRoot;
    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatHistory;
    @FXML private Button chatSend;
    @FXML private ChoiceBox<String> chatEnterAircraft;
    @FXML private ChoiceBox<String> chatEnterCommand;
    @FXML private TextField chatEnterParam;

    @FXML
    public void initialize(){
        Platform.runLater(() -> resize());
        root.widthProperty().addListener((obs, oldVal, newVal) -> resize());
        root.heightProperty().addListener((obs, oldVal, newVal) -> resize());
        root.setOnMouseClicked(e -> resize());

        chatHistory.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1));

        chatSend.setOnAction(e -> sendMessage());
        populateChoiceBox();
    }

    private void resize(){
        int radarDimensions = Math.min((int)centerGrid.getHeight(), (int)centerGrid.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);
        rectRadarBg.setWidth(radarDimensions);
        rectRadarBg.setHeight(radarDimensions);
        chatRoot.setPrefSize(root.getWidth() - radarDimensions, 0);
    }

    private void sendMessage(){
        String msg = chatEnterAircraft.getValue() + chatEnterCommand.getValue() + chatEnterParam.getText();
        Label msgLabel = new Label(msg);
        msgLabel.setFont(new Font("Arial", 32));
        chatHistory.getChildren().add(msgLabel);
    }

    public void populateChoiceBox(){
        chatEnterAircraft.setItems(FXCollections.observableArrayList("Boeing", "Airbus", "Cessna"));
        chatEnterCommand.setItems(FXCollections.observableArrayList("Cleared to land", "Maintain", "Turn left heading", "Reduce speed to"));
    }
}
