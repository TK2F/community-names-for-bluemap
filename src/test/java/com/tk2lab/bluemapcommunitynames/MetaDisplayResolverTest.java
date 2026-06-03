package com.tk2lab.bluemapcommunitynames;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MetaDisplayResolverTest {
    private final PluginSettings.SanitizeSettings sanitizeSettings = new PluginSettings.SanitizeSettings(
            true,
            true,
            true,
            true,
            Normalizer.Form.NFC,
            64,
            true
    );

    @Test
    void noMetaValuesUsesPlayerName() {
        PlayerEntry entry = resolve(Map.of(), fields("community_name"));

        assertEquals("JavaPlayer", entry.display());
        assertEquals(0, entry.metaValues().size());
    }

    @Test
    void oneMetaValueAppends() {
        PlayerEntry entry = resolve(Map.of("community_name", "CommunityName"), fields("community_name"));

        assertEquals("JavaPlayer/CommunityName", entry.display());
        assertEquals(List.of("CommunityName"), values(entry));
    }

    @Test
    void twoMetaValuesAppendInConfiguredOrder() {
        PlayerEntry entry = resolve(Map.of(
                "community_name", "CommunityName",
                "title", "ProjectOwner"
        ), fields("community_name", "title"));

        assertEquals("JavaPlayer/CommunityName/ProjectOwner", entry.display());
        assertEquals(List.of("CommunityName", "ProjectOwner"), values(entry));
    }

    @Test
    void threeMetaValuesAppendInConfiguredOrder() {
        PlayerEntry entry = resolve(Map.of(
                "community_name", "CommunityName",
                "title", "ProjectOwner",
                "role", "Builder"
        ), fields("community_name", "title", "role"));

        assertEquals("JavaPlayer/CommunityName/ProjectOwner/Builder", entry.display());
        assertEquals(List.of("CommunityName", "ProjectOwner", "Builder"), values(entry));
    }

    @Test
    void missingMiddleFieldDoesNotCreateDoubleSeparator() {
        PlayerEntry entry = resolve(Map.of(
                "community_name", "CommunityName",
                "role", "Builder"
        ), fields("community_name", "title", "role"));

        assertEquals("JavaPlayer/CommunityName/Builder", entry.display());
    }

    @Test
    void rosterAliasAndChipsUseArbitraryMetaKeys() {
        PlayerEntry entry = resolve(Map.of(
                "guild_name", "MoonGuild",
                "event_rank", "Champion"
        ), rosterFields(
                new PluginSettings.DisplayField("guild", "guild_name", "Guild", "alias", true, true, true),
                new PluginSettings.DisplayField("event", "event_rank", "Event", "chip", true, false, true)
        ));

        assertEquals("MoonGuild", entry.displayName());
        assertEquals("@JavaPlayer", entry.subName());
        assertEquals(List.of("Champion"), entry.chips().stream().map(ResolvedMetaValue::value).toList());
        assertEquals(List.of("MoonGuild", "Champion"), values(entry));
    }

    @Test
    void noConfiguredFieldsUsesPlayerNameOnlyRoster() {
        PlayerEntry entry = resolve(Map.of(
                "community_name", "Ignored",
                "title", "Ignored"
        ), rosterFields());

        assertEquals("JavaPlayer", entry.display());
        assertEquals("JavaPlayer", entry.displayName());
        assertEquals(null, entry.subName());
        assertEquals(List.of(), entry.metaValues());
        assertEquals(List.of(), entry.chips());
    }

    @Test
    void allConfiguredFieldsMissingUsesPlayerNameOnlyRoster() {
        PlayerEntry entry = resolve(Map.of(), rosterFields(
                new PluginSettings.DisplayField("nickname", "nickname", "Nickname", "alias", true, true, true),
                new PluginSettings.DisplayField("class_name", "class_name", "Class", "chip", true, false, true)
        ));

        assertEquals("JavaPlayer", entry.displayName());
        assertEquals(null, entry.subName());
        assertEquals(List.of(), entry.metaValues());
        assertEquals(List.of(), entry.chips());
    }

    @Test
    void maliciousLookingValueIsSanitizedAndStillTextData() {
        PlayerEntry entry = resolve(Map.of("community_name", " <script>alert(1)</script>\n "), fields("community_name"));

        assertEquals("JavaPlayer/<script>alert(1)</script>", entry.display());
        assertEquals("<script>alert(1)</script>", entry.metaValues().get(0).value());
    }

    private PlayerEntry resolve(Map<String, String> meta, PluginSettings.DisplaySettings displaySettings) {
        return MetaDisplayResolver.resolve(
                "JavaPlayer",
                displaySettings,
                meta::get,
                sanitizeSettings,
                false
        );
    }

    private PluginSettings.DisplaySettings fields(String... keys) {
        return new PluginSettings.DisplaySettings(
                "/",
                3,
                Arrays.stream(keys)
                        .map(key -> new PluginSettings.DisplayField(key, key, true, true))
                        .toList(),
                false
        );
    }

    private PluginSettings.DisplaySettings rosterFields(PluginSettings.DisplayField... fields) {
        return new PluginSettings.DisplaySettings(
                "/",
                3,
                Arrays.stream(fields).toList(),
                false,
                "community_name_as_primary",
                true,
                "@"
        );
    }

    private List<String> values(PlayerEntry entry) {
        return entry.metaValues().stream().map(ResolvedMetaValue::value).toList();
    }
}
