package com.atc.client.controller;

import com.atc.client.model.Airplane;
import com.atc.client.model.GameActivity;
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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import static com.atc.client.Dimensions.CANVAS_HEIGHT;
import static com.atc.client.Dimensions.CANVAS_WIDTH;


public class GameActivityController extends GenericController {
    public GameActivity gameActivity;


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


    /*DEBUG_1*/

    public class TimeMover_DEBUG implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Platform.runLater(() -> {
                gameActivity.moveAirplanes_DEBUG();

                gameActivity.wrapPrinting();

                gameActivity.printAllDots();

                gameActivity.gameCanvas.resize_canvas(radar);

            });

        }
    }

    public Timer t;
    public ActionListener listener;

    /*end of DEBUG_1*/

    @FXML
    public void initialize(){
        gameActivity = new GameActivity();
        gameActivity.setRadar(radar);

        Platform.runLater(this::resize);
        root.widthProperty().addListener((obs, oldVal, newVal) -> resize());
        root.heightProperty().addListener((obs, oldVal, newVal) -> resize());
        chatHistory.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1));

        /*DEBUG_2*/
        listener = new TimeMover_DEBUG();
        t = new Timer(100, listener);
        /*end of DEBUG_2*/

        chatSend.setOnAction(e -> {
            sendMessage();
            //gameCanvas.print_wrap_single(Integer.parseInt(chatEnterHeading.getText()), Integer.parseInt(chatEnterSpeed.getText()), Integer.parseInt(chatEnterLevel.getText()), 100, "GUWNO", radar);
            /*DEBUG_3*/
            t.start();
            /*end of DEBUG_3*/
            }
        );

        /*DEBUG_4*/
        for(int i = 0; i<10; i++){
            Airplane airplane = new Airplane(300,300);
            airplane.setMaxSpeed(1000);
            airplane.setMinSpeed(0);
            airplane.setCurrHeading(new Random().nextInt(360));
            airplane.setTargetHeading(airplane.getCurrHeading());
            airplane.setCurrHeight(new Random().nextInt(200)+200);
            airplane.setTargetHeight(airplane.getCurrHeight()+new Random().nextInt(400)-200);
            airplane.setCurrPosX(CANVAS_WIDTH/4+new Random().nextInt((int)CANVAS_WIDTH/2));
            airplane.setCurrPosY(CANVAS_HEIGHT/4+new Random().nextInt((int)CANVAS_HEIGHT/2));
            airplane.setCurrSpeed(200);
            airplane.setTargetSpeed(airplane.getCurrSpeed()+new Random().nextInt(100)-50);
            gameActivity.addAirplane(airplane);
        }
        /*end of DEBUG_4*/

        populateChoiceBox();

    }

    private void resize(){
        int radarDimensions = Math.min((int)centerGrid.getHeight(), (int)centerGrid.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);

        chatRoot.setPrefSize(root.getWidth() - radarDimensions, 0);

        gameActivity.resizeCanvas();
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
