package com.atc.client;

import com.atc.client.controller.WindowController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    //TODO: close game after user closes its window

    @Override
    public void start(Stage primaryStage) throws IOException, ClassNotFoundException {
        /*
        // HERE VVV
        Socket socket = new Socket("localhost", 2137);
        System.out.println("Connected!");
        System.out.println(socket.toString());

        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        // HERE ^^^

        for(int i = 0; i < 1000000; ++i) {
            Message msg = (Message) inputStream.readObject();
            System.out.println(msg.getAirplanes().size());
            msg.getAirplanes().forEach((k, airplane) -> {
               System.out.println(airplane.getUid());
            });
        }

        outputStream.writeObject(new Message("test"));
        System.out.println("cool");
        outputStream.writeObject(new Message("test"));
        System.out.println("cool");
        // HERE ^^^
        */
        WindowController mainWindowController = new WindowController(primaryStage);
        mainWindowController.loadAndSetScene("/fxml/MainActivity.fxml");

        primaryStage.setTitle("ATC Client");
        //primaryStage.getIcons().add(new Image("images/airplane.png"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
