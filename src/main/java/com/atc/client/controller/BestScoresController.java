package com.atc.client.controller;

import com.atc.client.model.ClientStreamHandler;
import com.atc.server.model.Login;
import com.atc.server.model.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

/**
 * Class controlling exchange of data about players with the database.
 */
public class BestScoresController extends GenericController{
    @FXML Button mainMenuButton;
    @FXML TableColumn loginCol;
    @FXML TableColumn pointsCol;
    @FXML TableColumn airplanesCol;
    @FXML TableColumn gameIDCol;
    List<Player> players;
    List <Login> logins;
    @FXML TableView playersTableView = new TableView();

    /**
     * Class container used to display data in playersTableView
     */
    public class PlayerEntry{
        String login;
        int points;
        int airplanesNum;
        int gameID;

        /**
         * Instantiates a new Player entry.
         *
         * @param login        the login
         * @param points       the points
         * @param airplanesNum the airplanes num
         * @param gameID       the game id
         */
        public PlayerEntry(String login, int points, int airplanesNum,int gameID) {
            this.login = login;
            this.points = points;
            this.airplanesNum = airplanesNum;
            this.gameID = gameID;
        }

        /**
         * Gets game id - necessary for TableView to work.
         *
         * @return the game id
         */
        public int getGameID() {
            return gameID;
        }

        /**
         * Gets login - necessary for TableView to work.
         *
         * @return the login
         */
        public String getLogin() {
            return login;
        }

        /**
         * Gets points - necessary for TableView to work.
         *
         * @return the points
         */
        public int getPoints() {
            return points;
        }

        /**
         * Gets airplanes num necessary for TableView to work.
         *
         * @return the airplanes num
         */
        public int getAirplanesNum() {
            return airplanesNum;
        }
    }

    public void initialize(){
        try {
            getDataAboutPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }
        createColumns();
        mainMenuButton.setOnAction(e-> windowController.loadAndSetScene("/fxml/MainActivity.fxml", gameSettings));
    }

    /**
     * Manages data exchange with database. Writes a message to stream asking for data about players, waits for it
     * to be sent and then uploads it.
     */

    private void getDataAboutPlayers() throws IOException {
        try {
            ClientStreamHandler.getInstance().setStreamState(ClientStreamHandler.StreamStates.STREAM_HISTORY);
            ClientStreamHandler.getInstance().askForPlayers();
        }catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Database connection");
            alert.setHeaderText(null);
            alert.setContentText("Unexpected error occurred.");
            alert.showAndWait();
        }
        players = ClientStreamHandler.getInstance().getPlayersList();
        logins = ClientStreamHandler.getInstance().getBestScoresLoginsList();
        ClientStreamHandler.getInstance().setStreamState(ClientStreamHandler.StreamStates.STREAM_IDLE);
    }

    /**
     * Searches for a login corresponding to given playerID in the database.
     * @param playerId
     * @return login or null, whether a search is successful or not.
     */
    private Login searchForLogin(int playerId){
        for(Login login: logins){
            if(login.getPlayerId() == playerId)
                return login;
        }
        return null;
    }

    /**
     * Adds columns to TableView and populates them with data.
     */

    private void createColumns(){
        loginCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, String>("login"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, Integer>("points"));
        airplanesCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, Integer>("airplanesNum"));
        gameIDCol.setCellValueFactory(new PropertyValueFactory<PlayerEntry, Integer>("gameID"));
        populateTableView();
    }

    /**
     * Creates PlayerEntries for every row from database and puts it in TableView.
     */

    private void populateTableView(){
        final ObservableList<PlayerEntry> playerEntries =  FXCollections.observableArrayList();
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
