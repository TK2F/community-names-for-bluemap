package com.tk2lab.bluemapcommunitynames;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;

public record PluginSettings(
        String metaKey,
        RosterSettings roster,
        DisplaySettings display,
        WebSettings web,
        SanitizeSettings sanitize,
        UpdateSettings updates,
        boolean floodgateEnabled,
        boolean debugLogging,
        List<String> warnings
) {
    public static final String DEFAULT_META_KEY = "community_name";
    public static final int DEFAULT_MAX_FIELDS = 3;

    public static PluginSettings from(FileConfiguration config) {
        List<String> warnings = new ArrayList<>();
        String metaKey = normalizeMetaKey(config.getString("meta-key", DEFAULT_META_KEY));
        RosterSettings roster = parseRosterSettings(config, metaKey, warnings);
        return new PluginSettings(
                metaKey,
                roster,
                displaySettingsFrom(roster),
                new WebSettings(
                        config.getString("web.public-base-path", "/bcn/"),
                        config.getString("web.local-web-dir", "web"),
                        config.getString("web.script-file", "overlay.js"),
                        config.getString("web.style-file", "overlay.css"),
                        config.getString("web.json-file", "players.json"),
                        config.getString("web.panel-title", "Online Players"),
                        Math.max(1, config.getInt("web.poll-interval-seconds", 5))
                ),
                new SanitizeSettings(
                        config.getBoolean("sanitize.trim", true),
                        config.getBoolean("sanitize.strip-legacy-section-codes", true),
                        config.getBoolean("sanitize.remove-control-chars", true),
                        config.getBoolean("sanitize.remove-bidi-controls", true),
                        parseNormalize(config.getString("sanitize.normalize", "NFC")),
                        Math.max(1, config.getInt("sanitize.max-code-points", 24)),
                        config.getBoolean("sanitize.ellipsis-on-truncate", true)
                ),
                new UpdateSettings(
                        Math.max(0, config.getLong("updates.debounce-millis", 300L)),
                        Math.max(1, config.getLong("updates.periodic-full-refresh-seconds", 10L))
                ),
                config.getBoolean("floodgate.enabled", true),
                config.getBoolean("logging.debug", false),
                List.copyOf(warnings)
        );
    }

    public static String normalizeMetaKey(String configuredMetaKey) {
        if (configuredMetaKey == null || configuredMetaKey.isBlank()) {
            return DEFAULT_META_KEY;
        }
        return configuredMetaKey.trim();
    }

    private static DisplaySettings parseDisplaySettings(FileConfiguration config, List<String> warnings) {
        String separator = config.getString("display.separator", "/");
        if (separator == null || separator.isBlank()) {
            separator = "/";
            warnings.add("Config value display.separator is blank; falling back to '/'.");
        }

        int maxFields = Math.min(DEFAULT_MAX_FIELDS, Math.max(1, config.getInt("display.max-fields", DEFAULT_MAX_FIELDS)));
        boolean hasDisplayFields = config.isSet("display.fields");
        List<DisplayField> fields = hasDisplayFields
                ? parseConfiguredFields(config.getMapList("display.fields"), maxFields, warnings)
                : List.of();

        boolean fallbackUsed = false;
        if (fields.isEmpty()) {
            fallbackUsed = true;
            String metaKey = normalizeMetaKey(config.getString("meta-key", DEFAULT_META_KEY));
            if (hasDisplayFields) {
                warnings.add("No valid display.fields entries remain; falling back to " + DEFAULT_META_KEY + ".");
                metaKey = DEFAULT_META_KEY;
            } else if (config.getString("meta-key", DEFAULT_META_KEY) == null
                    || config.getString("meta-key", DEFAULT_META_KEY).isBlank()) {
                warnings.add("Config value meta-key is blank; falling back to " + DEFAULT_META_KEY + ".");
            }
            fields = List.of(new DisplayField(metaKey, metaKey, true, true));
        }

        return new DisplaySettings(separator, maxFields, fields, fallbackUsed);
    }

    private static RosterSettings parseRosterSettings(FileConfiguration config, String metaKey, List<String> warnings) {
        boolean explicitRosterFields = config.isSet("player-roster.luckperms-fields.fields");
        boolean explicitDisplayFields = config.isSet("display.fields");
        boolean explicitMetaKey = config.isSet("meta-key");

        int maxFields = Math.min(DEFAULT_MAX_FIELDS, Math.max(1,
                config.getInt("player-roster.luckperms-fields.max-fields",
                        config.getInt("display.max-fields", DEFAULT_MAX_FIELDS))));

        List<RosterField> fields;
        boolean fallbackUsed = false;
        if (explicitRosterFields) {
            List<Map<?, ?>> configuredFields = config.getMapList("player-roster.luckperms-fields.fields");
            fields = parseRosterFields(configuredFields, maxFields, warnings);
            if (!configuredFields.isEmpty() && fields.isEmpty()) {
                warnings.add("No valid player-roster.luckperms-fields entries remain; roster will use Minecraft IDs only.");
            }
        } else if (explicitDisplayFields) {
            List<Map<?, ?>> configuredFields = config.getMapList("display.fields");
            fields = parseConfiguredFields(configuredFields, maxFields, warnings).stream()
                    .map(field -> new RosterField(field.key(), field.key(), field.label(), "chip", true, field.filterable()))
                    .toList();
            if (!configuredFields.isEmpty() && fields.isEmpty()) {
                warnings.add("No valid display.fields entries remain; roster will use Minecraft IDs only.");
            }
        } else if (explicitMetaKey) {
            fallbackUsed = true;
            fields = List.of(new RosterField(metaKey, metaKey, metaKey, "alias", true, true));
        } else {
            fallbackUsed = true;
            fields = defaultRosterFields();
        }

        return new RosterSettings(
                config.getBoolean("player-roster.enabled", true),
                new RosterPanelSettings(
                        normalizeChoice(config.getString("player-roster.panel.default-state", "collapsed"),
                                Set.of("collapsed", "expanded"), "collapsed", "player-roster.panel.default-state", warnings),
                        normalizeChoice(config.getString("player-roster.panel.position", "top-right"),
                                Set.of("top-right", "top-left", "bottom-right", "bottom-left"), "top-right", "player-roster.panel.position", warnings),
                        config.getString("player-roster.panel.max-height", "520px"),
                        Math.max(1, config.getInt("player-roster.panel.max-height-vh", 70))
                ),
                new RosterNameDisplaySettings(
                        normalizeChoice(config.getString("player-roster.name-display.mode", "community_name_as_primary"),
                                Set.of("community_name_as_primary", "minecraft_id_as_primary", "minecraft_id_only"),
                                "community_name_as_primary", "player-roster.name-display.mode", warnings),
                        config.getBoolean("player-roster.name-display.show-minecraft-id-as-subtext", true),
                        config.getString("player-roster.name-display.minecraft-id-prefix", "@")
                ),
                new RosterSearchSettings(
                        config.getBoolean("player-roster.search.enabled", true),
                        config.getStringList("player-roster.search.targets").isEmpty()
                                ? List.of("minecraft_id", "alias")
                                : List.copyOf(config.getStringList("player-roster.search.targets"))
                ),
                new RosterDetailsSettings(
                        normalizeChoice(config.getString("player-roster.details.default-state", "expanded"),
                                Set.of("expanded", "collapsed"), "expanded", "player-roster.details.default-state", warnings),
                        config.getBoolean("player-roster.details.allow-toggle", true)
                ),
                new RosterFiltersSettings(
                        config.getBoolean("player-roster.filters.enabled", true),
                        config.getBoolean("player-roster.filters.collapsed-by-default", true),
                        config.getBoolean("player-roster.filters.field-sections-collapsed-by-default", true),
                        Math.max(1, config.getInt("player-roster.filters.max-visible-values-per-field", 8)),
                        Math.max(1, config.getInt("player-roster.filters.high-cardinality-threshold", 12))
                ),
                maxFields,
                fields,
                fallbackUsed
        );
    }

    private static List<RosterField> parseRosterFields(List<Map<?, ?>> configuredFields, int maxFields, List<String> warnings) {
        List<RosterField> fields = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        boolean aliasSeen = false;
        int enabledCount = 0;
        for (Map<?, ?> entry : configuredFields) {
            boolean enabled = booleanValue(entry.get("enabled"), true);
            if (!enabled) {
                continue;
            }
            enabledCount++;
            String key = stringValue(entry.get("meta-key"));
            if (key == null || key.isBlank()) {
                key = stringValue(entry.get("key"));
            }
            if (key == null || key.isBlank()) {
                warnings.add("Config value player-roster.luckperms-fields[].meta-key is blank; skipping field.");
                continue;
            }
            key = key.trim();
            String normalizedSeenKey = key.toLowerCase();
            if (!seenKeys.add(normalizedSeenKey)) {
                warnings.add("Duplicate roster meta key '" + key + "' skipped.");
                continue;
            }
            if (fields.size() >= maxFields) {
                continue;
            }
            String id = stringValue(entry.get("id"));
            if (id == null || id.isBlank()) {
                id = key;
            } else {
                id = id.trim();
            }
            String label = stringValue(entry.get("label"));
            if (label == null || label.isBlank()) {
                label = id;
                warnings.add("Roster field '" + key + "' has blank label; using id as label.");
            } else {
                label = label.trim();
            }
            String display = normalizeChoice(stringValue(entry.get("display")), Set.of("alias", "chip"), "chip",
                    "player-roster.luckperms-fields[].display", warnings);
            if ("alias".equals(display)) {
                if (aliasSeen) {
                    warnings.add("Only one roster alias field is supported; field '" + key + "' will display as a chip.");
                    display = "chip";
                } else {
                    aliasSeen = true;
                }
            }
            fields.add(new RosterField(
                    id,
                    key,
                    label,
                    display,
                    booleanValue(entry.get("searchable"), "alias".equals(display)),
                    booleanValue(entry.get("filterable"), true)
            ));
        }
        if (enabledCount > maxFields) {
            warnings.add("More than " + maxFields + " roster fields are enabled; using the first " + maxFields + " valid fields.");
        }
        return List.copyOf(fields);
    }

    private static List<RosterField> defaultRosterFields() {
        return List.of(
                new RosterField("community_name", "community_name", "よび名", "alias", true, true),
                new RosterField("title", "title", "称号", "chip", false, true),
                new RosterField("role", "role", "ロール", "chip", false, true)
        );
    }

    private static DisplaySettings displaySettingsFrom(RosterSettings roster) {
        return new DisplaySettings(
                "/",
                roster.maxFields(),
                roster.fields().stream()
                        .map(field -> new DisplayField(field.id(), field.key(), field.label(), field.display(), true, field.searchable(), field.filterable()))
                        .toList(),
                roster.fallbackUsed(),
                roster.nameDisplay().mode(),
                roster.nameDisplay().showMinecraftIdAsSubtext(),
                roster.nameDisplay().minecraftIdPrefix()
        );
    }

    private static List<DisplayField> parseConfiguredFields(List<Map<?, ?>> configuredFields, int maxFields, List<String> warnings) {
        List<DisplayField> fields = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int enabledCount = 0;
        for (Map<?, ?> entry : configuredFields) {
            boolean enabled = booleanValue(entry.get("enabled"), true);
            if (!enabled) {
                continue;
            }
            enabledCount++;
            String key = stringValue(entry.get("key"));
            if (key == null || key.isBlank()) {
                warnings.add("Config value display.fields[].key is blank; skipping field.");
                continue;
            }
            key = key.trim();
            String normalizedSeenKey = key.toLowerCase();
            if (!seen.add(normalizedSeenKey)) {
                warnings.add("Duplicate display field key '" + key + "' skipped.");
                continue;
            }
            if (fields.size() >= maxFields) {
                continue;
            }
            String label = stringValue(entry.get("label"));
            if (label == null || label.isBlank()) {
                label = key;
                warnings.add("Display field '" + key + "' has blank label; using key as label.");
            } else {
                label = label.trim();
            }
            fields.add(new DisplayField(key, label, true, booleanValue(entry.get("filterable"), true)));
        }
        if (enabledCount > maxFields) {
            warnings.add("More than " + maxFields + " display fields are enabled; using the first " + maxFields + " valid fields.");
        }
        return List.copyOf(fields);
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean booleanValue(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private static String normalizeChoice(String value, Set<String> allowed, String defaultValue, String path, List<String> warnings) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim();
        if (allowed.contains(normalized)) {
            return normalized;
        }
        warnings.add("Config value " + path + " is invalid; using " + defaultValue + ".");
        return defaultValue;
    }

    private static Normalizer.Form parseNormalize(String value) {
        if (value == null || value.isBlank() || "NONE".equalsIgnoreCase(value)) {
            return null;
        }
        return Normalizer.Form.valueOf(value.trim().toUpperCase());
    }

    public record DisplaySettings(
            String separator,
            int maxFields,
            List<DisplayField> fields,
            boolean fallbackUsed,
            String nameDisplayMode,
            boolean showMinecraftIdAsSubtext,
            String minecraftIdPrefix
    ) {
        public DisplaySettings {
            fields = List.copyOf(fields);
        }

        public DisplaySettings(String separator, int maxFields, List<DisplayField> fields, boolean fallbackUsed) {
            this(separator, maxFields, fields, fallbackUsed, "community_name_as_primary", true, "@");
        }
    }

    public record RosterSettings(
            boolean enabled,
            RosterPanelSettings panel,
            RosterNameDisplaySettings nameDisplay,
            RosterSearchSettings search,
            RosterDetailsSettings details,
            RosterFiltersSettings filters,
            int maxFields,
            List<RosterField> fields,
            boolean fallbackUsed
    ) {
        public RosterSettings {
            fields = List.copyOf(fields);
        }
    }

    public record RosterPanelSettings(
            String defaultState,
            String position,
            String maxHeight,
            int maxHeightVh
    ) {
    }

    public record RosterNameDisplaySettings(
            String mode,
            boolean showMinecraftIdAsSubtext,
            String minecraftIdPrefix
    ) {
    }

    public record RosterSearchSettings(
            boolean enabled,
            List<String> targets
    ) {
        public RosterSearchSettings {
            targets = List.copyOf(targets);
        }
    }

    public record RosterDetailsSettings(
            String defaultState,
            boolean allowToggle
    ) {
    }

    public record RosterFiltersSettings(
            boolean enabled,
            boolean collapsedByDefault,
            boolean fieldSectionsCollapsedByDefault,
            int maxVisibleValuesPerField,
            int highCardinalityThreshold
    ) {
    }

    public record RosterField(
            String id,
            String key,
            String label,
            String display,
            boolean searchable,
            boolean filterable
    ) {
    }

    public record DisplayField(
            String id,
            String key,
            String label,
            String display,
            boolean enabled,
            boolean searchable,
            boolean filterable
    ) {
        public DisplayField(String key, String label, boolean enabled, boolean filterable) {
            this(key, key, label, "chip", enabled, true, filterable);
        }
    }

    public record WebSettings(
            String publicBasePath,
            String localWebDir,
            String scriptFile,
            String styleFile,
            String jsonFile,
            String panelTitle,
            int pollIntervalSeconds
    ) {
        public String normalizedPublicBasePath() {
            String value = publicBasePath == null || publicBasePath.isBlank() ? "/bcn/" : publicBasePath.trim();
            if (!value.startsWith("/")) value = "/" + value;
            if (!value.endsWith("/")) value = value + "/";
            return value;
        }

        public String scriptRegistrationPath() {
            return stripLeadingSlash(normalizedPublicBasePath()) + scriptFile;
        }

        public String styleRegistrationPath() {
            return stripLeadingSlash(normalizedPublicBasePath()) + styleFile;
        }

        public String jsonPublicUrl() {
            return normalizedPublicBasePath() + jsonFile;
        }

        private String stripLeadingSlash(String value) {
            return value.startsWith("/") ? value.substring(1) : value;
        }
    }

    public record SanitizeSettings(
            boolean trim,
            boolean stripLegacySectionCodes,
            boolean removeControlChars,
            boolean removeBidiControls,
            Normalizer.Form normalize,
            int maxCodePoints,
            boolean ellipsisOnTruncate
    ) {
    }

    public record UpdateSettings(long debounceMillis, long periodicFullRefreshSeconds) {
    }
}
