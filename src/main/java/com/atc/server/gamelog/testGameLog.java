package com.atc.server.gamelog;
import com.atc.server.model.*;
import java.util.List;
import java.util.UUID;

public class testGameLog {
    public static void main(String[] args){
        gameLog log = new gameLog();
        log.insertPlayer("Bartuś", 0, 0, 0);
        log.insertPlayer("Madzia", 200, 0, 10);
        log.insertPlayer("Rafaello", 2137, 10, 1000);
        List<Player> Players= log.selectAllPlayers();
        System.out.println("Players List: ");
        for(Player p: Players)
            System.out.println(p);
        log.insertEvent(1, "MOVEMENT", 1, 1, 10,10,100,
                80,1200, UUID.randomUUID().toString());
        log.insertEvent(1, "COLLISION", 1, 1, 10,10,100,
                80,1200, UUID.randomUUID().toString());
        log.insertEvent(4, "COMMAND", 4, 1, 10,10,100,
                80,1200, UUID.randomUUID().toString());
        List<Event> Events = log.selectAllEvents();
        System.out.println("ALL EVENTS:");
        for(Event e: Events)
            System.out.println(e);
        List<Event> otherEvents = log.selectGameIdEvents(4,4);
        System.out.println("EVENTS FROM GAME_ID = 4 AND TIME TICK = 4 :");
        for(Event e: otherEvents)
            System.out.println(e);
        log.closeConnection();
    }
}
