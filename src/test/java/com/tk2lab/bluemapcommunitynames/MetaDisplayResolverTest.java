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
        PlayerEntry entry = resolve(Map.of(), fields("alias_key"));

        assertEquals("player_alpha", entry.display());
        assertEquals(0, entry.metaValues().size());
    }

    @Test
    void oneMetaValueAppends() {
        PlayerEntry entry = resolve(Map.of("alias_key", "Alias Alpha"), fields("alias_key"));

        assertEquals("player_alpha/Alias Alpha", entry.display());
        assertEquals(List.of("Alias Alpha"), values(entry));
    }

    @Test
    void twoMetaValuesAppendInConfiguredOrder() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Alias Alpha",
                "guild_key", "Guild Sample"
        ), fields("alias_key", "guild_key"));

        assertEquals("player_alpha/Alias Alpha/Guild Sample", entry.display());
        assertEquals(List.of("Alias Alpha", "Guild Sample"), values(entry));
    }

    @Test
    void threeMetaValuesAppendInConfiguredOrder() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Alias Alpha",
                "guild_key", "Guild Sample",
                "role_key", "Role Sample"
        ), fields("alias_key", "guild_key", "role_key"));

        assertEquals("player_alpha/Alias Alpha/Guild Sample/Role Sample", entry.display());
        assertEquals(List.of("Alias Alpha", "Guild Sample", "Role Sample"), values(entry));
    }

    @Test
    void missingMiddleFieldDoesNotCreateDoubleSeparator() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Alias Alpha",
                "role_key", "Role Sample"
        ), fields("alias_key", "guild_key", "role_key"));

        assertEquals("player_alpha/Alias Alpha/Role Sample", entry.display());
    }

    @Test
    void rosterAliasAndChipsUseArbitraryMetaKeys() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Alias Alpha",
                "guild_key", "Guild Sample"
        ), rosterFields(
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        ));

        assertEquals("Alias Alpha", entry.displayName());
        assertEquals("@player_alpha", entry.subName());
        assertEquals(List.of("Guild Sample"), entry.chips().stream().map(ResolvedMetaValue::value).toList());
        assertEquals(List.of("Alias Alpha", "Guild Sample"), values(entry));
    }

    @Test
    void noConfiguredFieldsUsesPlayerNameOnlyRoster() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Ignored",
                "guild_key", "Ignored"
        ), rosterFields());

        assertEquals("player_alpha", entry.display());
        assertEquals("player_alpha", entry.displayName());
        assertEquals(null, entry.subName());
        assertEquals(List.of(), entry.metaValues());
        assertEquals(List.of(), entry.chips());
    }

    @Test
    void allConfiguredFieldsMissingUsesPlayerNameOnlyRoster() {
        PlayerEntry entry = resolve(Map.of(), rosterFields(
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        ));

        assertEquals("player_alpha", entry.displayName());
        assertEquals(null, entry.subName());
        assertEquals(List.of(), entry.metaValues());
        assertEquals(List.of(), entry.chips());
    }

    @Test
    void maliciousLookingValueIsSanitizedAndStillTextData() {
        PlayerEntry entry = resolve(Map.of("alias_key", " <script>alert(1)</script>\n "), fields("alias_key"));

        assertEquals("player_alpha/<script>alert(1)</script>", entry.display());
        assertEquals("<script>alert(1)</script>", entry.metaValues().get(0).value());
    }

    @Test
    void aliasAsPrimaryUsesAliasMainAndMinecraftIdSubtext() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Alias Alpha",
                "guild_key", "Guild Sample"
        ), rosterFields("alias_as_primary", true, true, false,
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        ));

        assertEquals("Alias Alpha", entry.displayName());
        assertEquals("@player_alpha", entry.subName());
        assertEquals(List.of("Guild Sample"), entry.chips().stream().map(ResolvedMetaValue::value).toList());
    }

    @Test
    void aliasAsPrimaryFallsBackWhenAliasMissingBlankOrSanitizedEmpty() {
        PluginSettings.DisplaySettings settings = rosterFields("alias_as_primary", true, true, false,
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        );

        assertEquals("player_alpha", resolve(Map.of("guild_key", "Guild Sample"), settings).displayName());
        assertEquals("player_alpha", resolve(Map.of("alias_key", "  ", "guild_key", "Guild Sample"), settings).displayName());
        assertEquals("player_alpha", resolve(Map.of("alias_key", "\u202E", "guild_key", "Guild Sample"), settings).displayName());
    }

    @Test
    void aliasAsPrimaryDoesNotDuplicateAliasThatEqualsMinecraftId() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "PLAYER_ALPHA",
                "guild_key", "Guild Sample"
        ), rosterFields("alias_as_primary", true, true, false,
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        ));

        assertEquals("player_alpha", entry.displayName());
        assertEquals(null, entry.subName());
        assertEquals(List.of("Guild Sample"), entry.chips().stream().map(ResolvedMetaValue::value).toList());
    }

    @Test
    void minecraftIdAsPrimaryKeepsIdMainAndAliasSubtext() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Alias Alpha",
                "guild_key", "Guild Sample"
        ), rosterFields("minecraft_id_as_primary", true, true, false,
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        ));

        assertEquals("player_alpha", entry.displayName());
        assertEquals("Alias Alpha", entry.subName());
        assertEquals(List.of("Guild Sample"), entry.chips().stream().map(ResolvedMetaValue::value).toList());
    }

    @Test
    void minecraftIdAsPrimaryCanShowAliasAsChipWhenSubtextDisabled() {
        PlayerEntry entry = resolve(Map.of(
                "alias_key", "Alias Alpha",
                "guild_key", "Guild Sample"
        ), rosterFields("minecraft_id_as_primary", true, false, true,
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        ));

        assertEquals("player_alpha", entry.displayName());
        assertEquals(null, entry.subName());
        assertEquals(List.of("Alias Alpha", "Guild Sample"), entry.chips().stream().map(ResolvedMetaValue::value).toList());
    }

    @Test
    void minecraftIdOnlyOmitsAliasUnlessChipIsExplicitlyEnabled() {
        PluginSettings.DisplaySettings hiddenAlias = rosterFields("minecraft_id_only", true, true, false,
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        );
        PluginSettings.DisplaySettings aliasChip = rosterFields("minecraft_id_only", true, true, true,
                new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true),
                new PluginSettings.DisplayField("guild", "guild_key", "Guild", "chip", true, false, true)
        );
        Map<String, String> meta = Map.of("alias_key", "Alias Alpha", "guild_key", "Guild Sample");

        PlayerEntry hidden = resolve(meta, hiddenAlias);
        PlayerEntry shown = resolve(meta, aliasChip);

        assertEquals("player_alpha", hidden.displayName());
        assertEquals(null, hidden.subName());
        assertEquals(List.of("Guild Sample"), hidden.chips().stream().map(ResolvedMetaValue::value).toList());
        assertEquals("player_alpha", shown.displayName());
        assertEquals(null, shown.subName());
        assertEquals(List.of("Alias Alpha", "Guild Sample"), shown.chips().stream().map(ResolvedMetaValue::value).toList());
    }

    @Test
    void bedrockStyleNameIsResolvedIndependentlyFromJavaStyleName() {
        PlayerEntry javaEntry = MetaDisplayResolver.resolve(
                "player_alpha",
                rosterFields("minecraft_id_as_primary", true, true, false,
                        new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true)),
                Map.of("alias_key", "Alias Alpha")::get,
                sanitizeSettings,
                false
        );
        PlayerEntry bedrockEntry = MetaDisplayResolver.resolve(
                ".player_alpha",
                rosterFields("minecraft_id_as_primary", true, true, false,
                        new PluginSettings.DisplayField("alias", "alias_key", "Alias", "alias", true, true, true)),
                Map.of("alias_key", "Alias Bedrock")::get,
                sanitizeSettings,
                true
        );

        assertEquals("player_alpha", javaEntry.displayName());
        assertEquals("Alias Alpha", javaEntry.subName());
        assertEquals(".player_alpha", bedrockEntry.displayName());
        assertEquals("Alias Bedrock", bedrockEntry.subName());
        assertEquals(true, bedrockEntry.bedrock());
    }

    private PlayerEntry resolve(Map<String, String> meta, PluginSettings.DisplaySettings displaySettings) {
        return MetaDisplayResolver.resolve(
                "player_alpha",
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
        return rosterFields("alias_as_primary", true, true, false, fields);
    }

    private PluginSettings.DisplaySettings rosterFields(String mode, boolean showMinecraftIdAsSubtext,
                                                       boolean showAliasAsSubtext, boolean showAliasAsChip,
                                                       PluginSettings.DisplayField... fields) {
        return new PluginSettings.DisplaySettings(
                "/",
                3,
                Arrays.stream(fields).toList(),
                false,
                mode,
                showMinecraftIdAsSubtext,
                showAliasAsSubtext,
                showAliasAsChip,
                "@"
        );
    }

    private List<String> values(PlayerEntry entry) {
        return entry.metaValues().stream().map(ResolvedMetaValue::value).toList();
    }
}
