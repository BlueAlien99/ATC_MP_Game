package com.atc.client.controller;

import com.atc.client.model.Airplane;
import com.atc.client.model.GameActivity;
import com.atc.server.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import com.atc.server.model.streamReader;
import java.io.IOException;
import java.util.UUID;


public class GameActivityController extends GenericController {
    public GameActivity gameActivity;
    public streamReader s;


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
        gameActivity = new GameActivity();
        gameActivity.setRadar(radar);

        gameActivity.gameCanvas.radarAirplanes.setOnMouseClicked(e -> {
            double xPos = e.getX();
            double yPos = e.getY();
            gameActivity.setActive(xPos, yPos, gameSettings.getClientUUID());
        });

        //TODO: This, as with all uses of gameCanvas canvases has to be rewrritten
//        gameActivity.gameCanvas.radarAirplanes.setOnScroll(e -> {
//            double delta = 1.2;
//
//            double scale = gameActivity.gameCanvas.radarAirplanes.getScaleX();
//            double oldScale = scale;
//
//            if (e.getDeltaY() < 0)
//                scale /= delta;
//            else
//                scale *= delta;
//
//            double f = (scale / oldScale)-1;
//
//            double dx = (e.getSceneX() - (gameActivity.gameCanvas.radarAirplanes.getBoundsInParent().getWidth()/2 + gameActivity.gameCanvas.radarAirplanes.getBoundsInParent().getMinX()));
//            double dy = (e.getSceneY() - (gameActivity.gameCanvas.radarAirplanes.getBoundsInParent().getHeight()/2 + gameActivity.gameCanvas.radarAirplanes.getBoundsInParent().getMinY()));
//
//            gameActivity.gameCanvas.radarAirplanes.setScaleX( scale);
//            gameActivity.gameCanvas.radarAirplanes.setScaleY( scale);
//
//            gameActivity.gameCanvas.radarAirplanes.setTranslateX(gameActivity.gameCanvas.radarAirplanes.getTranslateX()-f*dx);
//            gameActivity.gameCanvas.radarAirplanes.setTranslateY(gameActivity.gameCanvas.radarAirplanes.getTranslateY()-f*dy);
//        });

        Platform.runLater(this::resize);
        root.widthProperty().addListener((obs, oldVal, newVal) -> resize());
        root.heightProperty().addListener((obs, oldVal, newVal) -> resize());
        chatHistory.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1));

        //TODO: The MAIN (i.e. this of running app instance) GameSettings should probably be static and in scope for all WindowControllers, however this also works for now lmao ~BJ
        Platform.runLater(()->{
            s = new streamReader(gameSettings, gameActivity, chatHistory);
            s.start();
        });

        chatSend.setOnAction(e -> {
//            sendMessage();

            //TODO: this should be moved from here
            int targetHeading=-1;
            int targetSpeed=-1;
            int targetLevel=-1;
            try {targetHeading =  Integer.parseInt(chatEnterHeading.getText());}
            catch(NumberFormatException ignored){}
            try {targetSpeed = Integer.parseInt(chatEnterSpeed.getText());}
            catch(NumberFormatException ignored){}
            try {targetLevel = Integer.parseInt(chatEnterLevel.getText());}
            catch(NumberFormatException ignored){}

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

//    private void sendMessage(){
//        String msg = gameActivity.getAirplaneByUUID(gameActivity.getActiveAirplane()).getId() + " HDG: " + chatEnterHeading.getText() + " KTS: " + chatEnterSpeed.getText() + " FL: " + chatEnterLevel.getText();
//        Label msgLabel = new Label(msg);
//        msgLabel.setFont(new Font("Comic Sans MS", 14));
//        chatHistory.getChildren().add(msgLabel);
//    }

    public void populateChoiceBox(){
        chatEnterAircraft.setItems(FXCollections.observableArrayList("Boeing", "Airbus", "Cessna"));
    }
}
