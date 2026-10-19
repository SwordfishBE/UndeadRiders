package net.undeadriders.spawn;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpawnDistanceTest {
    @Test
    void maximumDistanceIsRadialRatherThanPerAxis() {
        assertFalse(UndeadHorsemanSpawner.isWithinSpawnDistance(63, 63, 24, 64));
        assertTrue(UndeadHorsemanSpawner.isWithinSpawnDistance(0, 64, 24, 64));
        assertTrue(UndeadHorsemanSpawner.isWithinSpawnDistance(-24, 0, 24, 64));
        assertFalse(UndeadHorsemanSpawner.isWithinSpawnDistance(8, 8, 24, 64));
    }
}
