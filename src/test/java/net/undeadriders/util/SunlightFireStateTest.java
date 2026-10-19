package net.undeadriders.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SunlightFireStateTest {
    @Test
    void sunlightAloneCanBeRemovedInShade() {
        var fire = new SunlightFireState();
        fire.beginSunlight(0);
        fire.onTimerChange(0, 160);
        fire.endSunlight();
        fire.onTimerChange(160, 159);
        assertTrue(fire.active());
        assertEquals(0, fire.externalTicks());
    }

    @Test
    void shorterOverlappingExternalFireSurvivesSunlightRemoval() {
        var fire = new SunlightFireState();
        fire.beginSunlight(0);
        fire.endSunlight();
        fire.onExternalIgnition(40);
        fire.onTimerChange(160, 159);
        assertEquals(39, fire.externalTicks());
    }

    @Test
    void lavaAndDirectTimerChangesAreNotErased() {
        var fire = new SunlightFireState();
        fire.beginSunlight(60);
        fire.endSunlight();
        fire.onExternalIgnition(300);
        fire.onTimerChange(160, 300);
        fire.onTimerChange(300, 299);
        assertEquals(299, fire.externalTicks());
        fire.onTimerChange(299, 300);
        assertEquals(300, fire.externalTicks());
    }

    @Test
    void extinguishingOrExpiryClearsOwnership() {
        var fire = new SunlightFireState();
        fire.beginSunlight(0);
        fire.endSunlight();
        fire.onTimerChange(1, 0);
        assertFalse(fire.active());
    }

    @Test
    void reloadRestoresOwnershipAndBoundsExternalTimer() {
        var fire = new SunlightFireState();
        fire.restore(true, 40, 120);
        assertTrue(fire.active());
        assertEquals(40, fire.externalTicks());
        fire.restore(true, 999, 120);
        assertEquals(120, fire.externalTicks());
        fire.restore(false, 40, 120);
        assertFalse(fire.active());
    }
}
