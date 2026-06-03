package com.tk2lab.bluemapcommunitynames;

import java.text.Normalizer;

public final class Sanitizer {
    private static final int SECTION_SIGN = '\u00A7';
    private static final int ELLIPSIS = '\u2026';

    private Sanitizer() {
    }

    public static String sanitize(String input, PluginSettings.SanitizeSettings settings) {
        if (input == null) {
            return null;
        }

        String value = settings.trim() ? input.trim() : input;
        if (settings.normalize() != null) {
            value = Normalizer.normalize(value, settings.normalize());
        }
        if (settings.stripLegacySectionCodes()) {
            value = stripLegacyCodes(value);
        }
        value = removeConfiguredCodePoints(value, settings);
        value = truncate(value, settings.maxCodePoints(), settings.ellipsisOnTruncate());

        if (value.isBlank()) {
            return null;
        }
        return value;
    }

    private static String stripLegacyCodes(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ) {
            int cp = value.codePointAt(i);
            int width = Character.charCount(cp);
            if (cp == SECTION_SIGN && i + width < value.length()) {
                i += width + Character.charCount(value.codePointAt(i + width));
                continue;
            }
            out.appendCodePoint(cp);
            i += width;
        }
        return out.toString();
    }

    private static String removeConfiguredCodePoints(String value, PluginSettings.SanitizeSettings settings) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ) {
            int cp = value.codePointAt(i);
            if (!shouldRemove(cp, settings)) {
                out.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }
        return out.toString();
    }

    private static boolean shouldRemove(int cp, PluginSettings.SanitizeSettings settings) {
        if (settings.removeControlChars() && (cp == '\r' || cp == '\n' || cp == '\t' || Character.isISOControl(cp))) {
            return true;
        }
        return settings.removeBidiControls()
                && (cp == 0x061C
                || cp == 0x200E
                || cp == 0x200F
                || (cp >= 0x202A && cp <= 0x202E)
                || (cp >= 0x2066 && cp <= 0x2069));
    }

    private static String truncate(String value, int maxCodePoints, boolean ellipsisOnTruncate) {
        int count = value.codePointCount(0, value.length());
        if (count <= maxCodePoints) {
            return value;
        }

        int target = ellipsisOnTruncate && maxCodePoints > 1 ? maxCodePoints - 1 : maxCodePoints;
        int end = value.offsetByCodePoints(0, target);
        String truncated = value.substring(0, end);
        return ellipsisOnTruncate && maxCodePoints > 1 ? truncated + new String(Character.toChars(ELLIPSIS)) : truncated;
    }
}
