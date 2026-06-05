package com.tk2lab.bluemapcommunitynames;

import java.text.Normalizer;
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
        boolean aliasDuplicatesMinecraftId = sameDisplayValue(alias, playerName);

        List<ResolvedMetaValue> chips = new ArrayList<>();

        String displayName;
        String subName = null;
        if ("minecraft_id_as_primary".equals(mode)) {
            displayName = playerName;
            if (displaySettings.showAliasAsSubtext() && !aliasDuplicatesMinecraftId) {
                subName = alias;
            }
            chips.addAll(chips(metaValues, displaySettings.showAliasAsChip(), displayName, subName));
        } else if ("minecraft_id_only".equals(mode)) {
            displayName = playerName;
            chips.addAll(chips(metaValues, displaySettings.showAliasAsChip(), displayName, subName));
        } else {
            displayName = alias == null || alias.isBlank() || aliasDuplicatesMinecraftId ? playerName : alias;
            if (!aliasDuplicatesMinecraftId && alias != null && !alias.isBlank() && displaySettings.showMinecraftIdAsSubtext()) {
                subName = displaySettings.minecraftIdPrefix() + playerName;
            }
            chips.addAll(chips(metaValues, false, displayName, subName));
        }
        return new RosterNames(displayName, subName, chips);
    }

    private static List<ResolvedMetaValue> chips(List<ResolvedMetaValue> metaValues, boolean includeAlias, String displayName, String subName) {
        return metaValues.stream()
                .filter(metaValue -> includeAlias || !"alias".equals(metaValue.display()))
                .filter(metaValue -> !sameDisplayValue(metaValue.value(), displayName))
                .filter(metaValue -> !sameDisplayValue(metaValue.value(), subName))
                .toList();
    }

    private static boolean sameDisplayValue(String left, String right) {
        String normalizedLeft = normalizeDisplayValue(left);
        String normalizedRight = normalizeDisplayValue(right);
        return normalizedLeft != null && normalizedRight != null && normalizedLeft.equalsIgnoreCase(normalizedRight);
    }

    private static String normalizeDisplayValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFC);
        return normalized.isBlank() ? null : normalized;
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
