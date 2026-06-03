package com.tk2lab.bluemapcommunitynames;

import de.bluecolored.bluemap.api.BlueMapAPI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class BlueMapRegistrar {
    private final Logger logger;
    private Consumer<BlueMapAPI> listener;
    private Supplier<PluginSettings> settingsSupplier;
    private volatile String blueMapVersion = "unknown";
    private volatile String apiVersion = "unknown";
    private volatile Path diagnosticWebRoot;

    public BlueMapRegistrar(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void register(Supplier<PluginSettings> settingsSupplier) {
        if (listener != null) {
            return;
        }
        this.settingsSupplier = Objects.requireNonNull(settingsSupplier, "settingsSupplier");
        listener = api -> {
            PluginSettings settings = this.settingsSupplier.get();
            blueMapVersion = api.getBlueMapVersion();
            apiVersion = api.getAPIVersion();
            diagnosticWebRoot = api.getWebApp().getWebRoot();
            api.getWebApp().registerScript(settings.web().scriptRegistrationPath());
            api.getWebApp().registerStyle(settings.web().styleRegistrationPath());
            logger.info("Registered BlueMapCommunityNames web overlay with BlueMap.");
        };
        BlueMapAPI.onEnable(listener);
    }

    public void unregister() {
        if (listener != null) {
            BlueMapAPI.unregisterListener(listener);
            listener = null;
        }
        settingsSupplier = null;
    }

    public boolean detected() {
        return BlueMapAPI.getInstance().isPresent();
    }

    public String blueMapVersion() {
        return blueMapVersion;
    }

    public String apiVersion() {
        return apiVersion;
    }

    public Path diagnosticWebRoot() {
        return diagnosticWebRoot;
    }
}
