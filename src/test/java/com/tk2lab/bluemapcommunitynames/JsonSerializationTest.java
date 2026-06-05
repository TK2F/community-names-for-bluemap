package com.tk2lab.bluemapcommunitynames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonSerializationTest {
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Test
    void schemaFieldsArePresent() {
        PluginSettings.DisplaySettings displaySettings = displaySettings();
        PlayersDocument document = PlayersDocument.create(
                "2026-06-01T12:34:56Z",
                "0.2.0",
                "floodgate",
                displaySettings,
                List.of(new PlayerEntry(
                        ".BedrockPlayer",
                        ".BedrockPlayer/CommunityName",
                        true,
                        List.of(new ResolvedMetaValue("community_name", "Community", "CommunityName")),
                        ".BedrockPlayer CommunityName"
                ))
        );

        JsonObject json = JsonParser.parseString(gson.toJson(document)).getAsJsonObject();
        assertEquals(2, json.get("schemaVersion").getAsInt());
        assertEquals("2026-06-01T12:34:56Z", json.get("generatedAt").getAsString());
        assertEquals("0.2.0", json.get("pluginVersion").getAsString());
        assertEquals("floodgate", json.get("bedrockDetection").getAsString());
        assertEquals("/", json.getAsJsonObject("display").get("separator").getAsString());
        assertEquals("community_name", json.getAsJsonObject("display").getAsJsonArray("fields").get(0).getAsJsonObject().get("key").getAsString());
        assertTrue(json.getAsJsonArray("players").get(0).getAsJsonObject().get("bedrock").getAsBoolean());
        assertFalse(json.getAsJsonArray("players").get(0).getAsJsonObject().has("uuid"));
        assertEquals(".BedrockPlayer/CommunityName", json.getAsJsonArray("players").get(0).getAsJsonObject().get("displayName").getAsString());
        assertTrue(json.getAsJsonArray("players").get(0).getAsJsonObject().getAsJsonArray("chips").isEmpty());
        assertEquals("CommunityName", json.getAsJsonArray("players").get(0).getAsJsonObject()
                .getAsJsonArray("metaValues").get(0).getAsJsonObject().get("value").getAsString());
    }

    @Test
    void rosterFieldsAreSerializedForOverlay() {
        PlayersDocument document = PlayersDocument.create(
                "2026-06-01T12:34:56Z",
                "0.2.0",
                "floodgate",
                displaySettings(),
                new PluginSettings.RosterDetailsSettings("collapsed", true),
                new PluginSettings.RosterFiltersSettings(true, true, true, 8, 12),
                List.of(new PlayerEntry(
                        "JavaPlayer",
                        "JavaPlayer",
                        "CommunityName",
                        "@JavaPlayer",
                        "JavaPlayer/CommunityName/Builder",
                        false,
                        List.of(new ResolvedMetaValue("role", "role", "Role", "chip", false, true, "Builder")),
                        List.of(
                                new ResolvedMetaValue("community_name", "community_name", "Alias", "alias", true, true, "CommunityName"),
                                new ResolvedMetaValue("role", "role", "Role", "chip", false, true, "Builder")
                        ),
                        "JavaPlayer CommunityName"
                ))
        );

        JsonObject json = JsonParser.parseString(gson.toJson(document)).getAsJsonObject();
        JsonObject details = json.getAsJsonObject("roster").getAsJsonObject("details");
        JsonObject filters = json.getAsJsonObject("roster").getAsJsonObject("filters");
        JsonObject player = json.getAsJsonArray("players").get(0).getAsJsonObject();

        assertEquals("collapsed", details.get("defaultState").getAsString());
        assertTrue(details.get("allowToggle").getAsBoolean());
        assertEquals("alias_as_primary", json.getAsJsonObject("roster").get("nameDisplayMode").getAsString());
        assertTrue(json.getAsJsonObject("roster").get("showMinecraftIdAsSubtext").getAsBoolean());
        assertTrue(json.getAsJsonObject("roster").get("showAliasAsSubtext").getAsBoolean());
        assertFalse(json.getAsJsonObject("roster").get("showAliasAsChip").getAsBoolean());
        assertEquals(8, filters.get("maxVisibleValuesPerField").getAsInt());
        assertEquals(12, filters.get("highCardinalityThreshold").getAsInt());
        assertEquals("CommunityName", player.get("displayName").getAsString());
        assertEquals("@JavaPlayer", player.get("subName").getAsString());
        assertEquals("Builder", player.getAsJsonArray("chips").get(0).getAsJsonObject().get("value").getAsString());
        assertEquals("alias", player.getAsJsonArray("metaValues").get(0).getAsJsonObject().get("display").getAsString());
    }

    @Test
    void zeroRosterFieldsStillEmitDetailsAndPlayerNameOnlyData() {
        PlayersDocument document = PlayersDocument.create(
                "2026-06-01T12:34:56Z",
                "0.2.0",
                "floodgate",
                new PluginSettings.DisplaySettings(
                        "/",
                        3,
                        List.of(),
                        false,
                        "alias_as_primary",
                        true,
                        true,
                        false,
                        "@"
                ),
                new PluginSettings.RosterDetailsSettings("expanded", true),
                new PluginSettings.RosterFiltersSettings(true, true, true, 8, 12),
                List.of(new PlayerEntry("JavaPlayer", "JavaPlayer", false, List.of(), "JavaPlayer"))
        );

        JsonObject json = JsonParser.parseString(gson.toJson(document)).getAsJsonObject();
        JsonObject roster = json.getAsJsonObject("roster");
        JsonObject player = json.getAsJsonArray("players").get(0).getAsJsonObject();

        assertTrue(roster.getAsJsonArray("fields").isEmpty());
        assertEquals("expanded", roster.getAsJsonObject("details").get("defaultState").getAsString());
        assertEquals("JavaPlayer", player.get("displayName").getAsString());
        assertTrue(player.getAsJsonArray("chips").isEmpty());
        assertTrue(player.getAsJsonArray("metaValues").isEmpty());
    }

    @Test
    void playerSortingRuleIsDeterministic() {
        List<PlayerEntry> sorted = List.of(
                        new PlayerEntry("z", "z", null, List.of(), "z"),
                        new PlayerEntry("A", "A", false, List.of(), "A"),
                        new PlayerEntry("a", "a", true, List.of(), "a")
                ).stream()
                .sorted(Comparator.comparing(PlayerEntry::playerName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PlayerEntry::playerName))
                .toList();

        assertEquals(List.of("A", "a", "z"), sorted.stream().map(PlayerEntry::playerName).toList());
    }

    @Test
    void bedrockCanBeTrueFalseOrNull() {
        PlayersDocument document = PlayersDocument.create(
                "2026-06-01T12:34:56Z",
                "0.2.0",
                "floodgate",
                displaySettings(),
                List.of(
                        new PlayerEntry("a", "a", true, List.of(), "a"),
                        new PlayerEntry("b", "b", false, List.of(), "b"),
                        new PlayerEntry("c", "c", null, List.of(), "c")
                )
        );

        JsonObject json = JsonParser.parseString(gson.toJson(document)).getAsJsonObject();
        assertTrue(json.getAsJsonArray("players").get(0).getAsJsonObject().get("bedrock").getAsBoolean());
        assertFalse(json.getAsJsonArray("players").get(1).getAsJsonObject().get("bedrock").getAsBoolean());
        assertTrue(json.getAsJsonArray("players").get(2).getAsJsonObject().get("bedrock").isJsonNull());
    }

    private PluginSettings.DisplaySettings displaySettings() {
        return new PluginSettings.DisplaySettings(
                "/",
                3,
                List.of(
                        new PluginSettings.DisplayField("community_name", "community_name", "Alias", "alias", true, true, true),
                        new PluginSettings.DisplayField("role", "role", "Role", "chip", true, false, true)
                ),
                false,
                "alias_as_primary",
                true,
                true,
                false,
                "@"
        );
    }
}
