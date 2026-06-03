package com.tk2lab.bluemapcommunitynames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.text.Normalizer;
import org.junit.jupiter.api.Test;

class SanitizerTest {
    private final PluginSettings.SanitizeSettings settings = new PluginSettings.SanitizeSettings(
            true,
            true,
            true,
            true,
            Normalizer.Form.NFC,
            24,
            true
    );

    @Test
    void nullInputReturnsNull() {
        assertNull(Sanitizer.sanitize(null, settings));
    }

    @Test
    void blankInputReturnsNull() {
        assertNull(Sanitizer.sanitize(" \t\n ", settings));
    }

    @Test
    void japaneseTextIsPreserved() {
        assertEquals("コミュニティ名", Sanitizer.sanitize(" コミュニティ名 ", settings));
    }

    @Test
    void legacyColorCodesAreRemoved() {
        assertEquals("コミュニティ名", Sanitizer.sanitize("§aコミュ§lニティ名", settings));
    }

    @Test
    void controlCharactersAreRemoved() {
        assertEquals("abc", Sanitizer.sanitize("a\r\nb\tc", settings));
    }

    @Test
    void bidiControlsAreRemoved() {
        assertEquals("abc", Sanitizer.sanitize("a\u202Eb\u2066c", settings));
    }

    @Test
    void longUnicodeStringIsTruncatedByCodePoint() {
        String input = "😀".repeat(30);
        String result = Sanitizer.sanitize(input, settings);
        assertEquals(24, result.codePointCount(0, result.length()));
        assertEquals("…", result.substring(result.offsetByCodePoints(0, 23)));
    }

    @Test
    void htmlLookingStringIsPreservedForTextContentRendering() {
        assertEquals("<b onclick=x>", Sanitizer.sanitize("<b onclick=x>", settings));
    }
}
