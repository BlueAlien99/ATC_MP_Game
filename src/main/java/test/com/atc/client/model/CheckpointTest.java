package test.com.atc.client.model;

import com.atc.client.model.Airplane;
import com.atc.client.model.Checkpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointTest extends Checkpoint {

    Checkpoint checkpoint = null;
    Airplane airplane = null;

    @BeforeEach
    void init() {
        checkpoint = new Checkpoint(UUID.fromString("caac8660-f37b-4c49-96bc-e6f4d9ee6152"),
                0,10,0,0);
        airplane = new Airplane(UUID.fromString("3a412e99-598d-4a66-b6ce-57e1526e6797"),
                "XXXX", "XXXX", 100,100, 100,90, 100);
    }



    @Test
    void testPassingCheckpointMechanism() {
        assertFalse(checkpoint.getAirplane(airplane.getUuid()));
        checkpoint.addAirplane(airplane.getUuid());
        checkpoint.passAirplane(airplane.getUuid());
        assertTrue(checkpoint.getAirplane(airplane.getUuid()));
    }

    @Test
    void testSettersGetters() {
        assertEquals(UUID.fromString("caac8660-f37b-4c49-96bc-e6f4d9ee6152"), checkpoint.getCheckpointUUID());
        assertEquals(0, checkpoint.getGameID());
        assertEquals(10, checkpoint.getPoints());
        assertEquals(0, checkpoint.getxPos());
        assertEquals(0, checkpoint.getyPos());
        assertEquals(50, checkpoint.getRadius());
    }

}