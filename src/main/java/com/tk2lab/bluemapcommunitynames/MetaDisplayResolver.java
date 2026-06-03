package com.tk2lab.bluemapcommunitynames;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class MetaDisplayResolver {
    private MetaDisplayResolver() {
    }

    public static PlayerEntry resolve(
            String playerName,
            PluginSettings.DisplaySettings displaySettings,
            Function<String, String> metaLookup,
            PluginSettings.SanitizeSettings sanitizeSettings,
            Boolean bedrock
    ) {
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(displaySettings, "displaySettings");
        Objects.requireNonNull(metaLookup, "metaLookup");
        Objects.requireNonNull(sanitizeSettings, "sanitizeSettings");

        List<ResolvedMetaValue> metaValues = new ArrayList<>();
        for (PluginSettings.DisplayField field : displaySettings.fields()) {
            String value = Sanitizer.sanitize(metaLookup.apply(field.key()), sanitizeSettings);
            if (value != null && !value.isBlank()) {
                metaValues.add(new ResolvedMetaValue(
                        field.id(),
                        field.key(),
                        field.label(),
                        field.display(),
                        field.searchable(),
                        field.filterable(),
                        value
                ));
            }
        }

        String display = DisplayResolver.display(playerName, metaValues, displaySettings.separator());
        RosterNames rosterNames = rosterNames(playerName, displaySettings, metaValues);
        return new PlayerEntry(
                playerName,
                playerName,
                rosterNames.displayName(),
                rosterNames.subName(),
                display,
                bedrock,
                rosterNames.chips(),
                metaValues,
                filterText(playerName, display, rosterNames.displayName(), rosterNames.subName(), metaValues)
        );
    }

    private static RosterNames rosterNames(String playerName, PluginSettings.DisplaySettings displaySettings, List<ResolvedMetaValue> metaValues) {
        String mode = displaySettings.nameDisplayMode();
        String alias = metaValues.stream()
                .filter(metaValue -> "alias".equals(metaValue.display()))
                .map(ResolvedMetaValue::value)
                .findFirst()
                .orElse(null);

        List<ResolvedMetaValue> chips = metaValues.stream()
                .filter(metaValue -> "minecraft_id_only".equals(mode) || !"alias".equals(metaValue.display()))
                .toList();

        String displayName;
        String subName = null;
        if ("minecraft_id_as_primary".equals(mode)) {
            displayName = playerName;
            subName = alias;
        } else if ("minecraft_id_only".equals(mode)) {
            displayName = playerName;
        } else {
            displayName = alias == null || alias.isBlank() ? playerName : alias;
            if (alias != null && !alias.isBlank() && displaySettings.showMinecraftIdAsSubtext()) {
                displaySettings.minecraftIdPrefix();
                subName = displaySettings.minecraftIdPrefix() + playerName;
            }
        }
        return new RosterNames(displayName, subName, chips);
    }

    private static String filterText(String playerName, String display, String displayName, String subName, List<ResolvedMetaValue> metaValues) {
        StringBuilder filterText = new StringBuilder(playerName).append(' ').append(display).append(' ').append(displayName);
        if (subName != null && !subName.isBlank()) {
            filterText.append(' ').append(subName);
        }
        for (ResolvedMetaValue metaValue : metaValues) {
            if (metaValue.searchable() || "alias".equals(metaValue.display())) {
                filterText.append(' ').append(metaValue.value());
            }
        }
        return filterText.toString();
    }

    private record RosterNames(String displayName, String subName, List<ResolvedMetaValue> chips) {
    }
}
