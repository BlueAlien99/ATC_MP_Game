package com.atc.server.gamelog;
import com.atc.server.model.*;
import java.util.List;

public class testGameLog {
    public static void main(String[] args){
        gameLog log = new gameLog();
        log.insertPlayer("Bartuś", 0, 0, 0);
        log.insertPlayer("Madzia", 200, 0, 10);
        log.insertPlayer("Rafaello", 2137, 10, 1000);
        log.deleteFromPlayers(1);
        List<Player> Players= log.selectAllPlayers();
        System.out.println("Players List: ");
        for(Player p: Players)
            System.out.println(p);
        log.closeConnection();
    }
}
