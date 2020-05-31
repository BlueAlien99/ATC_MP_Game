package com.atc.client.controller;

import com.atc.client.model.Airplane;
import com.atc.client.model.Checkpoint;
import com.atc.client.model.GameCanvas;
import com.atc.client.model.GameSettings;
import com.atc.server.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class GameCreatorController extends GenericController{
    @FXML Button undoAirplaneButton;
    @FXML Button undoCheckpointButton;
    @FXML VBox gameCanvasVBox;
    @FXML BorderPane gameCreatorBorderPane;
    @FXML TextField chatEnterTime;
    @FXML TextField chatEnterPoints;
    @FXML TextField chatEnterHeading;
    @FXML TextField chatEnterAltitude;
    @FXML TextField chatEnterSpeed;
    @FXML Button mainMenuButton;
    @FXML Button startGameButton;
    @FXML GameCanvas radar;
    @FXML Button airplaneAddButton;
    @FXML Button checkpointAddButton;
    static DataFormat airplaneDF = new DataFormat("airplaneDataFormat");
    private Vector<Checkpoint> newCheckpoints = new Vector<>();
    private Vector<Airplane> newAirplanes = new Vector<>();
    private ConcurrentHashMap<UUID, Integer> airplaneDelays = new ConcurrentHashMap<>();
    private double spawnRatio = 10;

    public void initialize(){
        GameSettings.getInstance().setIpAddress("127.0.0.1");
        addGraphicToButtons("images/airplaneicon.png",
                "images/yellowballicon.png",
                "images/undoairplaneicon.png",
                "images/undoyellowballicon.png");
        mainMenuButton.setOnAction(e-> windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings));
        startGameButton.setOnAction(e->startGame());
        radar.setOnMouseClicked(e-> System.out.println(e.getX()+", "+e.getY()));
        Platform.runLater(this::resize);
    }


    private void resize(){
        int radarDimensions = Math.min((int)gameCanvasVBox.getHeight(),
                (int)gameCanvasVBox.getWidth());
        radar.setPrefSize(radarDimensions, radarDimensions);

        System.out.println("RadarDims: "+radarDimensions);
    }

    public void addGraphicToButtons(String airplaneImage, String checkpointImage,
                                    String undoAirplane, String undoCheckpoint){
        class ButtonSetter{
            public void setGraphicOnButton(Button button, String imageName){
                ImageView iv = new ImageView(new Image(imageName));
                iv.fitHeightProperty().setValue(button.getMaxHeight());
                iv.fitWidthProperty().setValue(button.getMaxWidth());
                button.setGraphic(iv);
            }
        }
        ButtonSetter bt = new ButtonSetter();
        bt.setGraphicOnButton(airplaneAddButton, airplaneImage);
        bt.setGraphicOnButton(checkpointAddButton, checkpointImage);
        bt.setGraphicOnButton(undoAirplaneButton, undoAirplane);
        bt.setGraphicOnButton(undoCheckpointButton, undoCheckpoint);
    }

    private void createAlert(String header, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

    }

    public void checkpointDragDone(DragEvent dragEvent) {
        System.out.println("DRAG CHECKPOINT DONE");
    }

    public void airplaneDragDone(DragEvent dragEvent) {
        System.out.println("DRAG AIRPLANE DONE");
    }

    public void dragOver(DragEvent dragEvent) {
        if (dragEvent.getGestureSource() != radar &&
                (dragEvent.getDragboard().hasString() || dragEvent.getDragboard().hasContent(airplaneDF))) {
            System.out.println("DRAG OVER CANVAS");
            dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        dragEvent.consume();
    }

    public void dragDropped(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasString()) {
            newCheckpoints.add(new Checkpoint(dragEvent.getX()/radar.xCoeff(),
                    dragEvent.getY()/radar.yCoeff(),
                    parseInt(db.getString())));
            radar.printCheckpoint(newCheckpoints.lastElement());
            radar.finish_printing();
            dragEvent.setDropCompleted(true);
        } else if (db.hasContent(airplaneDF)){
            System.out.println("ITS AN AIRPLANE!");
            Airplane draggedAirplane = (Airplane) db.getContent(airplaneDF);
            draggedAirplane.setPosX(dragEvent.getX()/radar.xCoeff());
            draggedAirplane.setPosY(dragEvent.getY()/radar.yCoeff());
            newAirplanes.add(draggedAirplane);
            radar.print_airplane(draggedAirplane);
            radar.finish_printing();
            dragEvent.setDropCompleted(true);
        }
        dragEvent.consume();
        System.out.println("DRAG DROPPED CANVAS");
    }

    public void checkpointDragDetected(MouseEvent event) {
        if(chatEnterPoints.getText().trim().isEmpty()){
            createAlert("Creator message", "One of the fields is empty. Please enter data!");
        }else {
            try{
                parseInt(chatEnterPoints.getText());
                System.out.println("DRAG DETECTED");
                Dragboard db = checkpointAddButton.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString(chatEnterPoints.getText());
                db.setContent(content);
                event.consume();
            } catch (Exception e){
                createAlert("Creator message", "Invalid points parameter. Remember it has to be an integer!");
            }
        }
    }

    public void airplaneDragDetected(MouseEvent event) {
        if(chatEnterAltitude.getText().trim().isEmpty()
                || chatEnterHeading.getText().trim().isEmpty()
                || chatEnterSpeed.getText().trim().isEmpty()
                || chatEnterTime.getText().trim().isEmpty()){
            createAlert("Creator message", "One of the fields is empty. Please enter data!");
        }else {
            try {
                Airplane dummyAirplane = new Airplane(gameSettings.getClientUUID(),
                        0, 0, parseDouble(chatEnterAltitude.getText()),
                        parseDouble(chatEnterHeading.getText()),
                        parseDouble(chatEnterSpeed.getText()));
                airplaneDelays.put(dummyAirplane.getUuid(), Integer.parseInt(chatEnterTime.getText()));
                System.out.println("DRAG DETECTED");
                event.setDragDetect(true);
                Dragboard db = airplaneAddButton.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.put(airplaneDF, dummyAirplane);
                db.setContent(content);
                event.consume();
            } catch (Exception e){
                createAlert("Creator message",
                        "One of the fields contain invalid data. " +
                                "Remember that airplane parameters should be numbers!");
            }
        }
    }
    public void undoAirplaneButtonClicked(MouseEvent event) {
        if(!newAirplanes.isEmpty()){
            radar.start_printing();
            newAirplanes.remove(newAirplanes.size()-1);
            for(Airplane airplane: newAirplanes){
                radar.print_airplane(airplane);
            }
            if(!newCheckpoints.isEmpty()){
                for(Checkpoint checkpoint: newCheckpoints){
                    radar.printCheckpoint(checkpoint);
                }
            }
            radar.finish_printing();
        }else
            createAlert("Creator message", "Nothing left to remove from canvas!");
    }

    public void undoCheckpointButtonClicked(MouseEvent event) {
        if(!newCheckpoints.isEmpty()){
            radar.start_printing();
            newCheckpoints.remove(newCheckpoints.size() -1);
            for (Checkpoint checkpoint: newCheckpoints) {
                radar.printCheckpoint(checkpoint);
            }
            if(!newAirplanes.isEmpty()){
                for(Airplane airplane: newAirplanes){
                    radar.print_airplane(airplane);
                }
            }
            radar.finish_printing();
        }else
            createAlert("Creator message", "Nothing left to remove from canvas!");
    }

    private void startGame(){
        ConcurrentHashMap<UUID, Airplane> msgAirplanes = new ConcurrentHashMap<>();
        ConcurrentHashMap<UUID, Checkpoint> msgCheckpoints = new ConcurrentHashMap<>();
        for(Airplane a : newAirplanes){
            if(airplaneDelays.get(a.getUuid())!=null) {
                a.moveAirplane(-airplaneDelays.get(a.getUuid()).doubleValue());
            }
            msgAirplanes.put(a.getUuid(), a);
        }
        for(Checkpoint c : newCheckpoints){
            msgCheckpoints.put(c.getCheckpointUUID(), c);
        }
        Message msg = new Message(Message.msgTypes.SEND_INITIAL);
        msg.setAirplanes(msgAirplanes);
        msg.setCheckpoints(msgCheckpoints);
        msg.setSpawnRatio(spawnRatio);
        msg.setGameSettings(GameSettings.getInstance());
        creatorMessage.msgSet=true;
        creatorMessage.msg=msg;
        windowController.loadAndSetScene("/fxml/GameActivity.fxml", GameSettings.getInstance());
    }

    public static class creatorMessage{
        public static boolean msgSet = false;
        public static Message msg = null;
    }
}

