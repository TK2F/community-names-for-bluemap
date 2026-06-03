package com.tk2lab.bluemapcommunitynames;

import java.util.List;

public final class DisplayResolver {
    private DisplayResolver() {
    }

    public static String display(String playerName, List<ResolvedMetaValue> metaValues, String separator) {
        String actualSeparator = separator == null || separator.isBlank() ? "/" : separator;
        StringBuilder display = new StringBuilder(playerName);
        for (ResolvedMetaValue metaValue : metaValues) {
            if (metaValue.value() != null && !metaValue.value().isBlank()) {
                display.append(actualSeparator).append(metaValue.value());
            }
        }
        return display.toString();
    }
}
