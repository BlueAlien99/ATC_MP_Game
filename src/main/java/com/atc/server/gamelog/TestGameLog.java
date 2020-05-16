package com.atc.server.gamelog;
import com.atc.server.model.*;
import java.util.List;
import java.util.UUID;

public class TestGameLog {
    public static void main(String[] args){
        GameLog log = new GameLog();
        System.out.println("Before connecting to database.");
        log.connect();
        System.out.println("After connecting to database.");
        List<Player> Players= log.selectAllPlayers();
        System.out.println("Players List: ");
        for(Player p: Players)
            System.out.println(p);

        log.insertEvent(1, "MOVEMENT", 1, UUID.randomUUID(), 10,10,100,
                80,1200, UUID.randomUUID());
        log.insertEvent(1, "COLLISION", 1, UUID.randomUUID(), 10,10,100,
                80,1200, UUID.randomUUID());
        log.insertEvent(4, "COMMAND", 4, UUID.randomUUID(), 10,10,100,
                80,1200, UUID.randomUUID());

        List<Event> Events = log.selectAllEvents();
        System.out.println("ALL EVENTS:");
        for(Event e: Events)
            System.out.println(e);
        List<Event> otherEvents = log.selectGameIdEvents(4,4);
        System.out.println("EVENTS FROM GAME_ID = 4 AND TIME TICK = 4 :");
        for(Event e: otherEvents)
            System.out.println(e);
        log.commit();
        System.out.println(log.selectGameId());
        log.closeConnection();
    }
}
