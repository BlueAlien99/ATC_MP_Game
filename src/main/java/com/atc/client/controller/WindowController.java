package com.atc.client.controller;

import com.atc.client.Dimensions;
import com.atc.client.model.GameSettings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class makes it easy to switch scenes in a window.
 */
public class WindowController {

    private Stage window;

    public WindowController(Stage stage){
        this.window = stage;
    }

    public void setScene(Scene scene){
        window.setScene(scene);
    }

    public void setScene(Pane pane, int width, int height){
        Font.loadFont(this.getClass().getResource("/fonts/Orbitron-VariableFont_wght.ttf").toExternalForm(), 10);
        Font.loadFont(this.getClass().getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 10).getFamily();
        Scene scene = new Scene(pane, width, height);
        scene.getStylesheets().add(this.getClass().getResource("/style/style.css").toExternalForm());
        setScene(scene);
    }

    public void setScene(Pane pane){
        setScene(pane, Dimensions.WINDOW_WIDTH, Dimensions.WINDOW_HEIGHT);
    }

    public GenericController loadAndSetScene(String layout, int width, int height, GameSettings gameSettings){
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource(layout));
        Pane pane = null;
        try{
            pane = loader.load();
        } catch(IOException e){
            e.printStackTrace();
        }
        GenericController controller = loader.getController();
        controller.setGameSettings(gameSettings);
        controller.setWindowController(this);
        setScene(pane, width, height);
        return controller;
    }

    public GenericController loadAndSetScene(String layout){
        GameSettings gameSettings = GameSettings.getInstance();
        return loadAndSetScene(layout, Dimensions.WINDOW_WIDTH, Dimensions.WINDOW_HEIGHT, gameSettings);
    }

    public GenericController loadAndSetScene(String layout, GameSettings gameSettings){
        return loadAndSetScene(layout, Dimensions.WINDOW_WIDTH, Dimensions.WINDOW_HEIGHT, gameSettings);
    }

    public void changeCursor(Cursor cursor){
        window.getScene().setCursor(cursor);
    }
}
