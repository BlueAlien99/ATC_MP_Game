package com.atc.client.controller;

import com.atc.client.model.*;
import com.atc.server.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class GameActivityController extends GenericController {

    private class TimeOutManager extends Thread{
        @Override
        public void run() {
            try {
                System.out.println("DADADDADADadadadadadadadad");
                s.streamNotifier.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!s.connected){
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Connection timed out");
                    alert.setHeaderText("We were unable to reach the server");
                    alert.setContentText("Please check the IP address in Game Settings or Firewall settings and try again.");
                    alert.showAndWait();
                    windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
                });
            }
        }
    }
    public GameActivity gameActivity;
    public StreamReader s;
    private Thread streamThread;
    public TimeOutManager t;

    @FXML private Pane root;
    @FXML private GridPane centerGrid;

    @FXML private GameCanvas radar;

    @FXML private Pane chatRoot;
    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatHistory;
    @FXML private Button chatSend;
    @FXML private ChoiceBox<String> chatEnterAircraft;
    @FXML private TextField chatEnterHeading;
    @FXML private TextField chatEnterSpeed;
    @FXML private TextField chatEnterAltitude;

    @FXML private MenuItem menuResume;
    @FXML private MenuItem menuReturn;

    @FXML
    public void initialize(){
        gameActivity = new GameActivity(this);
        gameActivity.setRadar(radar);

        radar.setOnMouseClicked(e -> {
            double xPos = e.getX()/radar.xCoeff();
            double yPos = e.getY()/radar.yCoeff();
            gameActivity.setActive(xPos, yPos, gameSettings.getClientUUID());
            Platform.runLater(
                    () -> gameActivity.wrapPrinting());

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
        System.out.println("XD: " + gameSettings);

        if(StreamController.checkInstance(StreamController.SC_TYPE_STREAMREADER)){
            s = (StreamReader) StreamController.getInstance();
        }
        else{
            s = (StreamReader) StreamController.setInstance(new StreamReader(gameSettings, gameActivity, chatHistory));
            streamThread = new Thread(s);
            streamThread.start();
        }

            t = new TimeOutManager();
            t.start();



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
                    s.out.writeObject(msgout);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if(changed != null) {
                    updateChatBoxes(changed.getTargetHeading(), changed.getTargetSpeed(), changed.getTargetAltitude());
                }
            }
        });


        menuResume.setOnAction(e-> {
                    try {
                        s.out.writeObject(new Message(Message.msgTypes.GAME_RESUME));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

        menuReturn.setOnAction(e->{
            try {
                s.out.writeObject(new Message(Message.msgTypes.CLIENT_GOODBYE));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", GameSettings.getInstance());
        });

        populateChoiceBox();
        System.out.println("GAC end of Initialzie");
        Platform.runLater(this::resize);

    }


    private void resize(){

        int radarDimensions = Math.min((int)centerGrid.getHeight(), (int)centerGrid.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);

        System.out.println("RadarDims: "+radarDimensions);

        chatRoot.setPrefSize(root.getWidth() - radarDimensions, 0);

        //gameActivity.resizeCanvas();
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

    public void updateChatBoxes(double heading, double speed, double altitude){
        chatEnterHeading.clear();
        chatEnterSpeed.clear();
        chatEnterAltitude.clear();
        chatEnterHeading.setPromptText(heading + " deg");
        chatEnterSpeed.setPromptText(speed + " kts");
        chatEnterAltitude.setPromptText(altitude + " ft");
    }

}
