package com.atc.client;

import com.atc.client.controller.WindowController;
import com.atc.client.model.ClientStreamHandler;
import com.atc.client.model.GameSettings;
import com.atc.server.ServerMain;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    ServerMain sm;
    GameSettings gs;
    ClientStreamHandler csh;

    //TODO: close game after user closes its window
    //TODO: remove useless buttons from header menu
    //TODO: player's score!
    //TODO: remove all commented and no longer used code, also styling

    @Override
    public void start(Stage primaryStage) {
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
        primaryStage.getIcons().add(new Image("images/airplane.png"));
        primaryStage.show();

        sm = ServerMain.getInstance();
        gs = GameSettings.getInstance();
        csh = ClientStreamHandler.getInstance();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
