package com.tk2lab.bluemapcommunitynames;

import java.util.List;

public record PlayerEntry(
        String playerName,
        String minecraftId,
        String displayName,
        String subName,
        String display,
        Boolean bedrock,
        List<ResolvedMetaValue> chips,
        List<ResolvedMetaValue> metaValues,
        String filterText
) {
    public PlayerEntry {
        chips = List.copyOf(chips);
        metaValues = List.copyOf(metaValues);
    }

    public PlayerEntry(String playerName, String display, Boolean bedrock, List<ResolvedMetaValue> metaValues, String filterText) {
        this(playerName, playerName, display, null, display, bedrock, List.of(), metaValues, filterText);
    }
}
