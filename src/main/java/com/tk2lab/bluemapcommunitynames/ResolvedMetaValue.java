package com.tk2lab.bluemapcommunitynames;

public record ResolvedMetaValue(
        String id,
        String key,
        String label,
        String display,
        boolean searchable,
        boolean filterable,
        String value
) {
    public ResolvedMetaValue(String key, String label, String value) {
        this(key, key, label, "chip", true, true, value);
    }
}
