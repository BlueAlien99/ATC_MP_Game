package com.atc.client;

import com.atc.client.controller.WindowController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage){

        WindowController mainWindowController = new WindowController(primaryStage);
        mainWindowController.loadAndSetScene("/fxml/MainActivity.fxml");

        primaryStage.setTitle("ATC Client");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
