package com.atc.client.controller;

import com.atc.client.model.Airplane;
import com.atc.client.model.Checkpoint;
import com.atc.client.model.GameCanvas;
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

import java.util.Vector;

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
    @FXML GameCanvas radar;
    @FXML Button airplaneAddButton;
    @FXML Button checkpointAddButton;
    private Vector<Checkpoint> newCheckpoints = new Vector<>();
    private Vector<Airplane> newAirplanes = new Vector<>();

    public void initialize(){
        addGraphicToButtons("images/airplaneicon.png",
                "images/yellowballicon.png",
                "images/undoairplaneicon.png",
                "images/undoyellowballicon.png");
        mainMenuButton.setOnAction(e-> windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings));
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
        System.out.println("DRAG OVER CANVAS");
        if (dragEvent.getGestureSource() != radar &&
                dragEvent.getDragboard().hasString()) {
            dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        dragEvent.consume();
    }

    public void dragDropped(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasString()) {
            System.out.println(db.getString());
            newCheckpoints.add(new Checkpoint(dragEvent.getX()/radar.xCoeff(),
                    dragEvent.getY()/radar.yCoeff(),
                    parseInt(db.getString())));
            radar.printCheckpoint(newCheckpoints.lastElement());
            radar.finish_printing();
            dragEvent.setDropCompleted(true);
        }
        dragEvent.consume();
        System.out.println("DRAG DROPPED CANVAS");
    }

    public void checkpointDragDetected(MouseEvent event) {
        if(chatEnterPoints.getText().trim().isEmpty()){
            createAlert("Creator message", "One of the field is empty. Please enter data!");
        }else {
            System.out.println("DRAG DETECTED");
            Dragboard db = checkpointAddButton.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            System.out.println(chatEnterPoints.getText());
            content.putString(chatEnterPoints.getText());
            db.setContent(content);
            event.consume();
        }
    }

    public void airplaneDragDetected(MouseEvent event) {
        if(chatEnterAltitude.getText().trim().isEmpty()
                || chatEnterHeading.getText().trim().isEmpty()
                || chatEnterSpeed.getText().trim().isEmpty()
                || chatEnterTime.getText().trim().isEmpty()){
            createAlert("Creator message", "One of the field is empty. Please enter data!");
        }else {
            event.setDragDetect(true);
            System.out.println("DRAG DETECTED");

            Dragboard db = airplaneAddButton.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(chatEnterAltitude.getText());
            content.putString(chatEnterHeading.getText());
            content.putString(chatEnterSpeed.getText());
            content.putString(chatEnterTime.getText());
            db.setContent(content);
            event.consume();
        }
    }
    public void undoAirplaneButtonClicked(MouseEvent event) {
    }

    public void undoCheckpointButtonClicked(MouseEvent event) {
        if(!newCheckpoints.isEmpty()){
            radar.start_printing();
            newCheckpoints.remove(newCheckpoints.size() -1);
            for (Checkpoint checkpoint: newCheckpoints) {
                radar.printCheckpoint(checkpoint);
            }
            radar.finish_printing();
        }else
            createAlert("Creator message", "Nothing to remove from canvas!");
    }
}

