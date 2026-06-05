package com.tk2lab.bluemapcommunitynames;

import java.util.List;

public record PlayersDocument(
        int schemaVersion,
        String generatedAt,
        String pluginVersion,
        String bedrockDetection,
        DisplayDocument display,
        RosterDocument roster,
        List<PlayerEntry> players
) {
    public static PlayersDocument create(
            String generatedAt,
            String pluginVersion,
            String bedrockDetection,
            PluginSettings.DisplaySettings displaySettings,
            PluginSettings.RosterDetailsSettings details,
            PluginSettings.RosterFiltersSettings filters,
            List<PlayerEntry> players
    ) {
        return new PlayersDocument(
                2,
                generatedAt,
                pluginVersion,
                bedrockDetection,
                DisplayDocument.from(displaySettings),
                RosterDocument.from(displaySettings, details, filters),
                List.copyOf(players)
        );
    }

    public static PlayersDocument create(
            String generatedAt,
            String pluginVersion,
            String bedrockDetection,
            PluginSettings.DisplaySettings displaySettings,
            PluginSettings.RosterFiltersSettings filters,
            List<PlayerEntry> players
    ) {
        return create(generatedAt, pluginVersion, bedrockDetection, displaySettings, null, filters, players);
    }

    public static PlayersDocument create(
            String generatedAt,
            String pluginVersion,
            String bedrockDetection,
            PluginSettings.DisplaySettings displaySettings,
            List<PlayerEntry> players
    ) {
        return create(generatedAt, pluginVersion, bedrockDetection, displaySettings, null, players);
    }

    public record DisplayDocument(
            String separator,
            List<FieldDocument> fields
    ) {
        public static DisplayDocument from(PluginSettings.DisplaySettings settings) {
            return new DisplayDocument(
                    settings.separator(),
                    settings.fields().stream()
                            .map(field -> new FieldDocument(field.id(), field.key(), field.label(), field.display(), field.searchable(), field.filterable()))
                            .toList()
            );
        }

        public DisplayDocument {
            fields = List.copyOf(fields);
        }
    }

    public record FieldDocument(
            String id,
            String key,
            String label,
            String display,
            boolean searchable,
            boolean filterable
    ) {
    }

    public record RosterDocument(
            String nameDisplayMode,
            boolean showMinecraftIdAsSubtext,
            boolean showAliasAsSubtext,
            boolean showAliasAsChip,
            String minecraftIdPrefix,
            DetailsDocument details,
            FilterSettingsDocument filters,
            List<FieldDocument> fields
    ) {
        public static RosterDocument from(PluginSettings.DisplaySettings settings) {
            return from(settings, null, null);
        }

        public static RosterDocument from(PluginSettings.DisplaySettings settings, PluginSettings.RosterDetailsSettings details, PluginSettings.RosterFiltersSettings filters) {
            return new RosterDocument(
                    settings.nameDisplayMode(),
                    settings.showMinecraftIdAsSubtext(),
                    settings.showAliasAsSubtext(),
                    settings.showAliasAsChip(),
                    settings.minecraftIdPrefix(),
                    DetailsDocument.from(details),
                    FilterSettingsDocument.from(filters),
                    settings.fields().stream()
                            .map(field -> new FieldDocument(field.id(), field.key(), field.label(), field.display(), field.searchable(), field.filterable()))
                            .toList()
            );
        }

        public RosterDocument {
            fields = List.copyOf(fields);
        }
    }

    public record DetailsDocument(
            String defaultState,
            boolean allowToggle
    ) {
        public static DetailsDocument from(PluginSettings.RosterDetailsSettings details) {
            if (details == null) {
                return new DetailsDocument("expanded", true);
            }
            return new DetailsDocument(details.defaultState(), details.allowToggle());
        }
    }

    public record FilterSettingsDocument(
            boolean enabled,
            boolean collapsedByDefault,
            boolean fieldSectionsCollapsedByDefault,
            int maxVisibleValuesPerField,
            int highCardinalityThreshold
    ) {
        public static FilterSettingsDocument from(PluginSettings.RosterFiltersSettings filters) {
            if (filters == null) {
                return new FilterSettingsDocument(true, true, true, 8, 12);
            }
            return new FilterSettingsDocument(
                    filters.enabled(),
                    filters.collapsedByDefault(),
                    filters.fieldSectionsCollapsedByDefault(),
                    filters.maxVisibleValuesPerField(),
                    filters.highCardinalityThreshold()
            );
        }
    }
}
