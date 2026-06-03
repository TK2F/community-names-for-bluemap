package com.tk2lab.bluemapcommunitynames;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DisplayResolverTest {
    @Test
    void noValuesUsesPlayerName() {
        assertEquals("JavaPlayer", DisplayResolver.display("JavaPlayer", List.of(), "/"));
    }

    @Test
    void multipleValuesAppendWithoutTrailingSeparator() {
        assertEquals("JavaPlayer/CommunityName/Builder", DisplayResolver.display(
                "JavaPlayer",
                List.of(
                        new ResolvedMetaValue("community_name", "Community", "CommunityName"),
                        new ResolvedMetaValue("role", "Role", "Builder")
                ),
                "/"
        ));
    }
}
