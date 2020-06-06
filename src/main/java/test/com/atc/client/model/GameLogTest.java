package test.com.atc.client.model;

import com.atc.client.model.Checkpoint;
import com.atc.server.gamelog.GameLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class GameLogTest extends GameLog {

    static GameLog log;
    @BeforeAll
    static void beforeAllTests(){
        log = new GameLog();
    }

    @BeforeEach
    void setUp() {
        log.cleanDatabase();
        System.out.println("Baza wyczyszczona!");
    }

    @Test
    void testInsertCheckpoints() {
        log.insertCheckpoints(UUID.randomUUID(), 0, 10,0,0,10);
        log.insertCheckpoints(UUID.randomUUID(), 0, 10,0,0,10);
        log.insertCheckpoints(UUID.randomUUID(), 0, 10,0,0,10);
        assertEquals(3,log.selectCheckpoints(0).size());
    }

    @Test
    void testCallsigns() {
        log.insertCallsign(0,UUID.randomUUID(), "X");
        log.insertCallsign(0, UUID.randomUUID(), "X");
        log.insertCallsign(0, UUID.randomUUID(), "X");
        log.insertCallsign(0, UUID.randomUUID(), "X");
        assertEquals(4, log.selectAirplaneCallsigns(0).size());
    }

    @Test
    void testGetPlayerIdFromDatabase() {
        assertEquals(0, getPlayerIdFromDatabase(UUID.randomUUID()));
        UUID playerUUID = UUID.randomUUID();
        insertEvent(0, "MOVEMENT", 0, playerUUID,10,
                "X", 0,0,0,0,0,UUID.randomUUID(),3);
        assertEquals(0,log.getPlayerIdFromDatabase(playerUUID));
    }

    @Test
    void testEvents() {
        insertEvent(0, "MOVEMENT", 1, UUID.randomUUID(), 10,
                "X", 0,0,0,0,0, UUID.randomUUID(), 1);
        insertEvent(1, "MOVEMENT", 1, UUID.randomUUID(), 10,
                "X", 0,0,0,0,0, UUID.randomUUID(), 1);
        insertEvent(3, "MOVEMENT", 1, UUID.randomUUID(), 10,
                "X",0,0,0,0,0, UUID.randomUUID(), 1);
        assertEquals(3, log.selectAllEvents().size());
        assertEquals(1, selectGameIdEvents(0).size());
        assertEquals(1, selectGameIdEvents(1).size());
        assertEquals(1, selectGameIdEvents(3).size());
    }

    @Test
    void testSelectGameId() {
        insertEvent(0, "X", 1, UUID.randomUUID(), 10,
                "X", 0,0,0,0,0, UUID.randomUUID(), 1);
        insertEvent(1, "X", 1, UUID.randomUUID(), 10,
                "X", 0,0,0,0,0, UUID.randomUUID(), 1);
        insertEvent(3, "X", 1, UUID.randomUUID(), 10,
                "X",0,0,0,0,0, UUID.randomUUID(), 1);
        assertEquals(3, selectGameId());
    }

    @Test
    void testSelectAvailableGameId() {
        insertEvent(0, "MOVEMENT", 1, UUID.randomUUID(), 10,
                "X", 0,0,0,0,0, UUID.randomUUID(), 1);
        insertEvent(1, "MOVEMENT", 1, UUID.randomUUID(), 10,
                "X", 0,0,0,0,0, UUID.randomUUID(), 1);
        insertEvent(3, "MOVEMENT", 1, UUID.randomUUID(), 10,
                "X",0,0,0,0,0, UUID.randomUUID(), 1);
        Vector<Integer> ids = log.selectAvailableGameId();
        assertEquals(3, ids.size());
        assertEquals(0, ids.get(0));
        assertEquals(1, ids.get(1));
        assertEquals(3, ids.get(2));
    }

    @Test
    void testSelectAirplaneCallsigns() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        insertCallsign(0, uuid1, "XYZ");
        insertCallsign(0,uuid2, "ABC");
        insertCallsign(0, uuid3, "KLM");
        HashMap<UUID, String> callsigns = log.selectAirplaneCallsigns(0);
        assertEquals(3, callsigns.size());
        callsigns.forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
        assertEquals("XYZ", callsigns.get(uuid1));
        assertEquals("ABC", callsigns.get(uuid2));
        assertEquals("KLM", callsigns.get(uuid3));
    }
    @Test
    void testDeleteFromPlayers() {
        assertTrue(log.insertPlayer(0, UUID.randomUUID(), 0, 0, 0));
        assertTrue(log.insertPlayer(0, UUID.randomUUID(), 0, 0, 0));
        assertTrue(log.insertPlayer(0, UUID.randomUUID(), 0, 0, 0));
        assertEquals(3, selectAllPlayers().size());
        log.deleteFromPlayers(1);
        log.deleteFromPlayers(2);
        log.deleteFromPlayers(3);
        assertEquals(0, selectAllPlayers().size());
    }

}