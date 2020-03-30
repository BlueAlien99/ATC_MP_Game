package com.atc.client.controller;

// Every ActivityController MUST extend this class in order to be compatible with WindowController.
public class GenericController {

    protected WindowController windowController;

    public void setWindowController(WindowController windowController) {
        this.windowController = windowController;
    }
}
