package com.tk2lab.bluemapcommunitynames;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonPublisherTest {
    @TempDir
    Path tempDir;

    @Test
    void writesTempFileAndMovesIntoPlace() throws Exception {
        JsonPublisher publisher = new JsonPublisher(Logger.getLogger("test"));
        try {
            Path target = tempDir.resolve("players.json");
            publisher.publishNow(PlayersDocument.create(
                    "2026-06-01T12:34:56Z",
                    "0.2.0",
                    "unavailable",
                    new PluginSettings.DisplaySettings(
                            "/",
                            3,
                            List.of(new PluginSettings.DisplayField("community_name", "Community", true, true)),
                            false
                    ),
                    List.of()
            ), target);

            assertTrue(Files.exists(target));
            assertTrue(Files.readString(target).contains("\"schemaVersion\":2"));
            assertNotNull(publisher.lastSuccessfulWrite());
        } finally {
            publisher.shutdown();
        }
    }
}
