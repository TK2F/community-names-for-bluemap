package com.tk2lab.bluemapcommunitynames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PluginSettingsTest {
    @Test
    void oldMetaKeyOnlyFallsBackToSingleDisplayField() {
        PluginSettings settings = settings("""
                meta-key: "bluemap_name"
                """);

        assertTrue(settings.display().fallbackUsed());
        assertEquals(List.of("bluemap_name"), keys(settings));
    }

    @Test
    void oldMetaKeyOnlyFallsBackEvenWhenDefaultsDefineDisplayFields() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new java.io.StringReader("""
                meta-key: "legacy_display"
                """));
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new java.io.StringReader("""
                display:
                  fields:
                    - key: "community_name"
                      label: "Community"
                      enabled: true
                      filterable: true
                """));
        config.setDefaults(defaults);

        PluginSettings settings = PluginSettings.from(config);

        assertTrue(settings.display().fallbackUsed());
        assertEquals(List.of("legacy_display"), keys(settings));
    }

    @Test
    void displayFieldsWinOverOldMetaKey() {
        PluginSettings settings = settings("""
                meta-key: "community_name"
                display:
                  fields:
                    - key: "title"
                      label: "Title"
                      enabled: true
                      filterable: true
                """);

        assertEquals(List.of("title"), keys(settings));
    }

    @Test
    void blankDuplicateAndTooManyFieldsAreWarnedAndNormalized() {
        PluginSettings settings = settings("""
                display:
                  fields:
                    - key: ""
                      enabled: true
                    - key: "community_name"
                      label: ""
                      enabled: true
                    - key: "community_name"
                      enabled: true
                    - key: "title"
                      enabled: true
                    - key: "role"
                      enabled: true
                    - key: "rank"
                      enabled: true
                """);

        assertEquals(List.of("community_name", "title", "role"), keys(settings));
        assertEquals("community_name", settings.display().fields().get(0).label());
        assertTrue(settings.warnings().stream().anyMatch(value -> value.contains("blank")));
        assertTrue(settings.warnings().stream().anyMatch(value -> value.contains("Duplicate")));
        assertTrue(settings.warnings().stream().anyMatch(value -> value.contains("More than")));
    }

    @Test
    void noValidDisplayFieldsUsesMinecraftIdOnlyMode() {
        PluginSettings settings = settings("""
                display:
                  fields:
                    - key: " "
                      enabled: true
                """);

        assertEquals(List.of(), keys(settings));
        assertTrue(settings.warnings().stream().anyMatch(value -> value.contains("Minecraft IDs only")));
    }

    @Test
    void explicitEmptyRosterFieldsAreRespected() {
        PluginSettings settings = settings("""
                player-roster:
                  luckperms-fields:
                    fields: []
                """);

        assertEquals(0, settings.display().fields().size());
        assertEquals(0, settings.roster().fields().size());
    }

    @Test
    void arbitraryRosterFieldNamesWorkWithoutCommunityName() {
        PluginSettings settings = settings("""
                player-roster:
                  luckperms-fields:
                    fields:
                      - id: guild
                        meta-key: guild_name
                        label: "Guild"
                        display: alias
                        searchable: true
                        filterable: true
                      - id: event
                        meta-key: event_rank
                        label: "Event"
                        display: chip
                        searchable: false
                        filterable: true
                """);

        assertEquals(List.of("guild_name", "event_rank"), keys(settings));
        assertEquals("alias", settings.display().fields().get(0).display());
        assertEquals("chip", settings.display().fields().get(1).display());
    }

    @Test
    void invalidOnlyRosterFieldsAreSafeMinecraftIdOnlyMode() {
        PluginSettings settings = settings("""
                player-roster:
                  luckperms-fields:
                    fields:
                      - meta-key: " "
                        label: "Blank"
                        display: alias
                """);

        assertEquals(List.of(), keys(settings));
        assertTrue(settings.warnings().stream().anyMatch(value -> value.contains("blank")));
        assertTrue(settings.warnings().stream().anyMatch(value -> value.contains("Minecraft IDs only")));
    }

    @Test
    void detailsSettingsDefaultToExpandedAndCanBeCollapsed() {
        PluginSettings defaults = settings("""
                player-roster:
                  luckperms-fields:
                    fields: []
                """);
        PluginSettings collapsed = settings("""
                player-roster:
                  details:
                    default-state: collapsed
                    allow-toggle: false
                  luckperms-fields:
                    fields:
                      - meta-key: guild_name
                        display: alias
                """);

        assertEquals("expanded", defaults.roster().details().defaultState());
        assertTrue(defaults.roster().details().allowToggle());
        assertEquals("collapsed", collapsed.roster().details().defaultState());
        assertEquals(false, collapsed.roster().details().allowToggle());
    }

    private PluginSettings settings(String yaml) {
        return PluginSettings.from(YamlConfiguration.loadConfiguration(new java.io.StringReader(yaml)));
    }

    private List<String> keys(PluginSettings settings) {
        return settings.display().fields().stream().map(PluginSettings.DisplayField::key).toList();
    }
}
