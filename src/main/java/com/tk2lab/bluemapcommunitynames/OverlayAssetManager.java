package com.tk2lab.bluemapcommunitynames;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class OverlayAssetManager {
    private final JavaPlugin plugin;

    public OverlayAssetManager(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void writeAssets(PluginSettings settings, Path webDir) throws IOException {
        Files.createDirectories(webDir);
        writeTemplate("web/overlay.js.template", webDir.resolve(settings.web().scriptFile()), settings);
        writeTemplate("web/overlay.css.template", webDir.resolve(settings.web().styleFile()), settings);
    }

    private void writeTemplate(String resource, Path target, PluginSettings settings) throws IOException {
        String content;
        try (InputStream input = plugin.getResource(resource)) {
            if (input == null) {
                throw new IOException("Missing resource " + resource);
            }
            content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        content = content
                .replace("__BCN_PANEL_TITLE__", jsString(settings.web().panelTitle()))
                .replace("__BCN_JSON_URL__", jsString(settings.web().jsonPublicUrl()))
                .replace("__BCN_POLL_MILLIS__", Long.toString(settings.web().pollIntervalSeconds() * 1000L));
        Files.writeString(target, content, StandardCharsets.UTF_8);
    }

    private static String jsString(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
