package com.atc.client.controller;

import com.atc.client.model.GameSettings;

// Every ActivityController MUST extend this class in order to be compatible with WindowController.
public class GenericController {

    protected WindowController windowController;

    protected GameSettings gameSettings;

    public void setGameSettings(GameSettings gameSettings){this.gameSettings = gameSettings;}

    public void setWindowController(WindowController windowController) {
        this.windowController = windowController;
    }
}
