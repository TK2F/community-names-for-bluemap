package com.tk2lab.bluemapcommunitynames;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public final class FloodgateBridge {
    private final boolean enabled;
    private final Logger logger;
    private Object api;
    private Method isFloodgatePlayer;

    public FloodgateBridge(boolean enabled, Logger logger) {
        this.enabled = enabled;
        this.logger = logger;
        reload();
    }

    public void reload() {
        api = null;
        isFloodgatePlayer = null;
        if (!enabled || !Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstance = apiClass.getMethod("getInstance");
            api = getInstance.invoke(null);
            isFloodgatePlayer = apiClass.getMethod("isFloodgatePlayer", UUID.class);
        } catch (ReflectiveOperationException | LinkageError ex) {
            logger.warning("Floodgate API is present but unavailable: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            api = null;
            isFloodgatePlayer = null;
        }
    }

    public Boolean bedrock(UUID uuid) {
        if (!available()) {
            return null;
        }
        try {
            return (Boolean) isFloodgatePlayer.invoke(api, uuid);
        } catch (ReflectiveOperationException | LinkageError ex) {
            return null;
        }
    }

    public boolean available() {
        return api != null && isFloodgatePlayer != null;
    }

    public String mode() {
        return available() ? "floodgate" : "unavailable";
    }
}
