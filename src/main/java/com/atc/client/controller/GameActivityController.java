package com.atc.client.controller;

import com.atc.client.model.Airplane;
import com.atc.client.model.GameCanvas;
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

import java.util.ArrayList;
import java.util.Random;


public class GameActivityController extends GenericController {

    public GameCanvas gameCanvas;

    public ArrayList<Airplane> airplanes = new ArrayList<>();


    @FXML private Pane root;
    @FXML private GridPane centerGrid;
    @FXML private StackPane radar;
    @FXML private Rectangle rectRadarBg;
    @FXML private Pane chatRoot;
    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatHistory;
    @FXML private Button chatSend;
    @FXML private ChoiceBox<String> chatEnterAircraft;
    @FXML private TextField chatEnterHeading;
    @FXML private TextField chatEnterSpeed;
    @FXML private TextField chatEnterLevel;

    @FXML
    public void initialize(){
        gameCanvas = new GameCanvas();

        Platform.runLater(this::resize);
        root.widthProperty().addListener((obs, oldVal, newVal) -> resize());
        root.heightProperty().addListener((obs, oldVal, newVal) -> resize());
        root.setOnMouseClicked(e -> resize());

        chatHistory.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1));

        /*debug only*/

        chatSend.setOnAction(e -> {
            sendMessage();
            //gameCanvas.print_wrap_single(Integer.parseInt(chatEnterHeading.getText()), Integer.parseInt(chatEnterSpeed.getText()), Integer.parseInt(chatEnterLevel.getText()), 100, "GUWNO", radar);
            for(Airplane airplane : airplanes){
                //every circa 8 steps we change heading by -180, 0 or 180
                if(new Random().nextBoolean() && new Random().nextBoolean() && new Random().nextBoolean()){
                    airplane.setTargetHeading(airplane.getCurrHeading()+(new Random().nextInt(3)-1)*180);
                }

                airplane.moveAirplane();
            }
            gameCanvas.print_airplanes_array(airplanes, radar);
        }
        );

        for(int i = 0; i<10; i++){
            airplanes.add(new Airplane(300,300));
        }


        for(Airplane airplane : airplanes){
            airplane.setMaxSpeed(1000);
            airplane.setMinSpeed(0);
            airplane.setCurrHeading(new Random().nextInt(360));
            airplane.setTargetHeading(airplane.getCurrHeading());
            airplane.setCurrHeight(new Random().nextInt(200)+200);
            airplane.setTargetHeight(airplane.getCurrHeight()+new Random().nextInt(400)-200);
            airplane.setCurrPosX(200+new Random().nextInt(400));
            airplane.setCurrPosY(200+new Random().nextInt(400));
            airplane.setCurrSpeed(200);
            airplane.setTargetSpeed(airplane.getCurrSpeed()+new Random().nextInt(100)-50);
        }

        populateChoiceBox();

    }

    private void resize(){
        int radarDimensions = Math.min((int)centerGrid.getHeight(), (int)centerGrid.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);

        chatRoot.setPrefSize(root.getWidth() - radarDimensions, 0);

        gameCanvas.resize_canvas(radar);
    }

    private void sendMessage(){
        String msg = chatEnterAircraft.getValue() + " " + chatEnterHeading.getText() + " " + chatEnterSpeed.getText() + " " + chatEnterLevel.getText();
        Label msgLabel = new Label(msg);
        msgLabel.setFont(new Font("Arial", 14));
        chatHistory.getChildren().add(msgLabel);
    }

    public void populateChoiceBox(){
        chatEnterAircraft.setItems(FXCollections.observableArrayList("Boeing", "Airbus", "Cessna"));
    }
}
