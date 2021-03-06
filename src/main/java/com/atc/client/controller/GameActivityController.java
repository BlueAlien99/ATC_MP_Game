package com.atc.client.controller;

import com.atc.client.model.*;
import com.atc.client.thread.ClientStreamHandler;
import com.atc.server.model.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class GameActivityController extends GenericController {

    private class TimeOutManager extends Thread{
        protected boolean timeouted = true;
        @Override
        public void run() {
            timeouted=false;
        }
    }
    public GameActivity gameActivity;
    public TimeOutManager t;

    @FXML private Pane root;
    @FXML private GridPane centerGrid;

    @FXML private GameCanvas radar;

    @FXML private Pane chatRoot;
    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatHistory;
    @FXML private Button chatSend;
    @FXML private TextField chatEnterHeading;
    @FXML private TextField chatEnterSpeed;
    @FXML private TextField chatEnterAltitude;

    @FXML private Button menuReturn;
    @FXML private Button menuResume;

    @FXML
    public void initialize(){

        gameActivity = new GameActivity(this);
        gameActivity.setRadar(radar);
        gameActivity.setClientUUID(GameSettings.getInstance().getClientUUID());

        radar.setOnMouseClicked(e -> {
            double xPos = e.getX()/radar.xCoeff();
            double yPos = e.getY()/radar.yCoeff();
            gameActivity.setActive(xPos, yPos);
            Platform.runLater(
                    () -> gameActivity.wrapPrinting());
        });

        root.widthProperty().addListener((obs) -> resize());
        root.heightProperty().addListener((obs) -> resize());
        chatHistory.heightProperty().addListener((obs, oldVal, newVal) -> chatScroll.setVvalue(1));

        TextField[] chatEnter = {chatEnterHeading, chatEnterAltitude, chatEnterSpeed};
        for (TextField textField : chatEnter) {
            textField.setOnKeyPressed(key -> {
                if (key.getCode().equals(KeyCode.ENTER)) {
                    chatSend.fire();
                }
            });
        }
        //System.out.println("XD: " + gameSettings);

        ClientStreamHandler.getInstance().setChatBox(chatHistory);
        ClientStreamHandler.getInstance().setGameActivity(gameActivity);

        try {
            ClientStreamHandler.getInstance().setStreamState(ClientStreamHandler.StreamStates.STREAM_GAME);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        t = new TimeOutManager();
        t.start();

        chatSend.setOnAction(e -> sendMessage());

        menuResume.setOnAction(e-> {
                    try {
                        if(!t.timeouted)
                            ClientStreamHandler.getInstance().writeMessage(new Message(Message.msgTypes.GAME_RESUME));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

        menuReturn.setOnAction(e->{
            try {
                if(!t.timeouted)
                    ClientStreamHandler.getInstance().writeMessage(new Message(Message.msgTypes.CLIENT_GOODBYE));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", GameSettings.getInstance());
        });

        //System.out.println("GAC end of Initialzie");
        Platform.runLater(this::resize);
    }

private void sendMessage(){
    int targetHeading=-1;
    int targetSpeed=-1;
    int targetLevel=-1;
    try {targetHeading =  Integer.parseInt(chatEnterHeading.getText());}
    catch(NumberFormatException ignored){}
    try {targetSpeed = Integer.parseInt(chatEnterSpeed.getText());}
    catch(NumberFormatException ignored){}
    try {targetLevel = Integer.parseInt(chatEnterAltitude.getText());}
    catch(NumberFormatException ignored){}

    if(gameActivity.getActiveAirplane() != null){
        Airplane changed = null;
        try {
            changed = (Airplane)gameActivity.getAirplaneByUUID(gameActivity.getActiveAirplane()).clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        if(changed != null && targetHeading!=-1)  changed.setTargetHeading(targetHeading);
        if(changed != null && targetSpeed!=-1)  changed.setTargetSpeed(targetSpeed);
        if(changed != null && targetLevel!=-1)  changed.setTargetAltitude(targetLevel);
        Message msgout = new Message(changed);
        try {
            if(!t.timeouted)
                ClientStreamHandler.getInstance().writeMessage(msgout);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if(changed != null) {
            updateChatBoxes(changed.getTargetHeading(), changed.getTargetSpeed(), changed.getTargetAltitude());
        }
    }
}
    private void resize(){

        int radarDimensions = Math.min((int)centerGrid.getHeight(), (int)centerGrid.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);

        //System.out.println("RadarDims: "+radarDimensions);

        chatRoot.setPrefSize(root.getWidth() - radarDimensions, 0);

        //gameActivity.resizeCanvas();
    }

    public void updateChatBoxes(double heading, double speed, double altitude){
        chatEnterHeading.clear();
        chatEnterSpeed.clear();
        chatEnterAltitude.clear();
        chatEnterHeading.setPromptText((int)heading + " deg");
        chatEnterSpeed.setPromptText((int)speed + " kts");
        chatEnterAltitude.setPromptText((int)altitude + " ft");
    }

    public void clearChatBoxes(){
        chatEnterHeading.clear();
        chatEnterSpeed.clear();
        chatEnterAltitude.clear();
        chatEnterHeading.setPromptText("Heading");
        chatEnterSpeed.setPromptText("Speed");
        chatEnterAltitude.setPromptText("Altitude");
    }

}
