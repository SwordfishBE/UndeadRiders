package net.undeadriders.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModrinthUpdateCheckerTest {
    @Test
    void ignoresOtherLoadersMinecraftVersionsAndPrereleases() {
        String response = """
            [
              {"version_number":"1.2.5","version_type":"release","date_published":"2026-10-01T00:00:00Z",
               "loaders":["fabric"],"game_versions":["26.3"]},
              {"version_number":"2.0","version_type":"release","date_published":"2026-10-02T00:00:00Z",
               "loaders":["neoforge"],"game_versions":["26.3"]},
              {"version_number":"3.0","version_type":"release","date_published":"2026-10-03T00:00:00Z",
               "loaders":["fabric"],"game_versions":["26.4"]},
              {"version_number":"4.0","version_type":"beta","date_published":"2026-10-04T00:00:00Z",
               "loaders":["fabric"],"game_versions":["26.3"]}
            ]
            """;
        assertEquals("1.2.5", ModrinthUpdateChecker.extractLatestVersion(response, "26.3").orElseThrow());
    }

    @Test
    void invalidRecordsDoNotAbortTheSearch() {
        String response = """
            [null, 42, {"version_number":{}},
             {"version_number":"broken","date_published":[]},
             {"version_number":"1.2.5","version_type":"release","date_published":"2026-10-01T00:00:00Z",
              "loaders":["fabric"],"game_versions":["26.3"]}]
            """;
        assertEquals("1.2.5", ModrinthUpdateChecker.extractLatestVersion(response, "26.3").orElseThrow());
    }

    @Test
    void nonArrayResponseHasNoUpdate() {
        assertTrue(ModrinthUpdateChecker.extractLatestVersion("{\"error\":\"unavailable\"}", "26.3").isEmpty());
    }
}
