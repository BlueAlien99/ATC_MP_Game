package com.atc.client.controller;

import com.atc.client.Dimensions;
import com.atc.client.model.Airplane;
import com.atc.client.model.GameActivity;
import com.atc.server.Message;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;


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

    public class streamReader extends Thread {

        protected ObjectInputStream in;
        public ObjectOutputStream out;
        protected Socket socket;

        @Override
        public void run()  {
            try {
                socket = new Socket(Dimensions.ipAddressDim, 2137);
                System.out.println("Connected!");
                System.out.println(socket.toString());
                in = new ObjectInputStream(socket.getInputStream());
                out  = new ObjectOutputStream(socket.getOutputStream());
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
            while(true){
                Message msg = null;
                try {
                    msg = (Message) in.readObject();
                    msg.getAirplanes().forEach((k, airplane) -> {
                        gameActivity.updateAirplane(airplane);
                    });
                    System.out.println("received data");
                }
                catch (IOException | ClassNotFoundException | NullPointerException ex) {
                    ex.printStackTrace();
                }
                Platform.runLater(
                        () -> {
                            gameActivity.wrapPrinting();
                        });
            }
        }
    }

    public streamReader s;



    @FXML
    public void initialize(){
        gameActivity = new GameActivity();
        gameActivity.setRadar(radar);

        UUID activePlane = null;

        gameActivity.gameCanvas.radarAirplanes.setOnMouseClicked(e -> {
            double xPos = e.getX();
            double yPos = e.getY();
            gameActivity.setActive(xPos, yPos);
        });

        Platform.runLater(this::resize);
        root.widthProperty().addListener((obs, oldVal, newVal) -> resize());
        root.heightProperty().addListener((obs, oldVal, newVal) -> resize());
        chatHistory.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1));

        s = new streamReader();
        s.start();


        chatSend.setOnAction(e -> {
            sendMessage();

            //TODO: this should be moved from here
            int targetHeading = Integer.parseInt(chatEnterHeading.getText());
            int targetSpeed = Integer.parseInt(chatEnterSpeed.getText());
            int targetLevel = Integer.parseInt(chatEnterLevel.getText());
            if(gameActivity.getActiveAirplane() != null){
                Airplane changed = null;
                try {
                    changed = (Airplane)gameActivity.getAirplaneByUUID(gameActivity.getActiveAirplane()).clone();
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }
                if(targetHeading!=-1)  changed.setTargetHeading(targetHeading);
                if(targetSpeed!=-1)  changed.setTargetSpeed(targetSpeed);
                if(targetLevel!=-1)  changed.setTargetHeight(targetLevel);
                Message msgout = new Message(changed);
                try {
                    s.out.writeObject(msgout);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
            }
        );

        populateChoiceBox();
    }

    private void resize(){
        int radarDimensions = Math.min((int)centerGrid.getHeight(), (int)centerGrid.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);

        chatRoot.setPrefSize(root.getWidth() - radarDimensions, 0);

        gameActivity.resizeCanvas();
    }

    private void sendMessage(){
        String msg = gameActivity.getAirplaneByUUID(gameActivity.getActiveAirplane()).getId() + " HDG: " + chatEnterHeading.getText() + " KTS: " + chatEnterSpeed.getText() + " FL: " + chatEnterLevel.getText();
        Label msgLabel = new Label(msg);
        msgLabel.setFont(new Font("Comic Sans MS", 14));
        chatHistory.getChildren().add(msgLabel);
    }

    public void populateChoiceBox(){
        chatEnterAircraft.setItems(FXCollections.observableArrayList("Boeing", "Airbus", "Cessna"));
    }
}
