package com.atc.client;

import com.atc.client.controller.WindowController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        Socket socket = new Socket("localhost", 2137);
        System.out.println("Connected!");
        System.out.println(socket.toString());

        WindowController mainWindowController = new WindowController(primaryStage);
        mainWindowController.loadAndSetScene("/fxml/MainActivity.fxml");

        primaryStage.setTitle("ATC Client");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
