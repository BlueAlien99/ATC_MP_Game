package com.atc.client;

import com.atc.client.controller.WindowController;
import com.atc.client.model.ClientStreamHandler;
import com.atc.client.model.GameSettings;
import com.atc.server.ServerMain;
import javafx.application.Application;
import javafx.application.Platform;
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
        WindowController mainWindowController = new WindowController(primaryStage);
        mainWindowController.loadAndSetScene("/fxml/MainActivity.fxml");

        primaryStage.setTitle("ATC Client");
        primaryStage.getIcons().add(new Image("images/airplane.png"));
        primaryStage.show();

        sm = ServerMain.getInstance();
        gs = GameSettings.getInstance();
        csh = ClientStreamHandler.getInstance();
    }

    @Override
    public void stop() throws Exception {
        ServerMain.getInstance().interrupt();
        ClientStreamHandler.getInstance().interrupt();
        Platform.exit();
        super.stop();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
