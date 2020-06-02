package com.atc.client.controller;

import com.atc.client.model.HistoryStream;
import com.atc.client.model.StreamController;
import com.atc.server.model.Login;
import com.atc.server.model.Player;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.List;

public class BestScoresController extends GenericController{
    @FXML Button mainMenuButton;
    @FXML TableColumn loginCol;
    @FXML TableColumn pointsCol;
    @FXML TableColumn airplanesCol;
    @FXML TableColumn gameIDCol;
    HistoryStream stream;
    Thread streamThread;
    List<Player> players;
    List <Login> logins;
    @FXML
    TableView playersTableView = new TableView();

    public class PlayerEntry{
        String login;
        int points;
        int airplanesNum;
        int gameID;

        public PlayerEntry(String login, int points, int airplanesNum,int gameID) {
            this.login = login;
            this.points = points;
            this.airplanesNum = airplanesNum;
            this.gameID = gameID;
        }

        public int getGameID() {
            return gameID;
        }

        public String getLogin() {
            return login;
        }

        public int getPoints() {
            return points;
        }

        public int getAirplanesNum() {
            return airplanesNum;
        }
    }

    public void initialize(){
        getDataAboutPlayers();
        createColumns();
        mainMenuButton.setOnAction(e->{
            windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings);
        });
    }


    private void getDataAboutPlayers(){
        stream = (HistoryStream) StreamController.setInstance(new HistoryStream("localhost"));
        streamThread = new Thread(stream);
        streamThread.start();
        try {
            stream.askForPlayers();
        }catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Database connection");
            alert.setHeaderText(null);
            alert.setContentText("Unexpected error occurred.");
            alert.showAndWait();
        }
        players = stream.getPlayersList();
        logins = stream.getBestScoresLoginsList();
        stream.sayGoodbye();
    }
    private Login searchForLogin(int playerId){
        for(Login login: logins){
            if(login.getPlayerId() == playerId)
                return login;
        }
        return null;
    }

    private void createColumns(){
        loginCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, String>("login"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, Integer>("points"));
        airplanesCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, Integer>("airplanesNum"));
        gameIDCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, Integer>("gameID"));
        populateTableView();
    }

    private void populateTableView(){
        final ObservableList<PlayerEntry> playerEntries =  FXCollections.observableArrayList();;
        if (logins!= null && players != null){
            for(Player player: players){
                Login login = searchForLogin(player.getIdPlayer());
                playerEntries.add(new PlayerEntry(login.getPlayerLogin(),
                        player.getPoints(), player.getAirplanesNum(), login.getGameID()));
            }
            playersTableView.setItems(playerEntries);
        }

    }
}
