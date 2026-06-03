package com.tk2lab.bluemapcommunitynames;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class BlueMapCommunityNamesPlugin extends JavaPlugin implements Listener {
    private PluginSettings settings;
    private LuckPermsService luckPermsService;
    private FloodgateBridge floodgateBridge;
    private PlayerSnapshotService snapshotService;
    private JsonPublisher jsonPublisher;
    private OverlayAssetManager assetManager;
    private BlueMapRegistrar blueMapRegistrar;
    private BukkitTask debounceTask;
    private BukkitTask periodicTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = PluginSettings.from(getConfig());
        warnConfigWarnings();
        luckPermsService = new LuckPermsService(this);
        luckPermsService.load();
        floodgateBridge = new FloodgateBridge(settings.floodgateEnabled(), getLogger());
        snapshotService = new PlayerSnapshotService(luckPermsService, floodgateBridge, Clock.systemUTC(), getDescription().getVersion());
        jsonPublisher = new JsonPublisher(getLogger());
        assetManager = new OverlayAssetManager(this);
        blueMapRegistrar = new BlueMapRegistrar(getLogger());

        try {
            assetManager.writeAssets(settings, webDir());
        } catch (IOException ex) {
            getLogger().warning("Failed to generate web assets: " + ex.getMessage());
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        luckPermsService.subscribe(uuid -> requestDebouncedRebuild());
        blueMapRegistrar.register(() -> settings);
        registerCommand();
        startPeriodicRefresh();
        rebuildNow();
    }

    @Override
    public void onDisable() {
        if (debounceTask != null) {
            debounceTask.cancel();
            debounceTask = null;
        }
        if (periodicTask != null) {
            periodicTask.cancel();
            periodicTask = null;
        }
        if (blueMapRegistrar != null) {
            blueMapRegistrar.unregister();
        }
        if (luckPermsService != null) {
            luckPermsService.close();
        }
        if (jsonPublisher != null) {
            jsonPublisher.shutdown();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        settings = PluginSettings.from(getConfig());
        warnConfigWarnings();
        floodgateBridge.reload();
        try {
            assetManager.writeAssets(settings, webDir());
        } catch (IOException ex) {
            getLogger().warning("Failed to regenerate web assets: " + ex.getMessage());
        }
        startPeriodicRefresh();
        rebuildNow();
    }

    public void rebuildNow() {
        runOnMainThread(() -> {
            PlayersDocument document = snapshotService.snapshot(settings);
            jsonPublisher.publishAsync(document, playersJsonPath());
        });
    }

    public void requestDebouncedRebuild() {
        runOnMainThread(() -> {
            if (debounceTask != null) {
                debounceTask.cancel();
            }
            long ticks = Math.max(1L, Math.round(settings.updates().debounceMillis() / 50.0d));
            debounceTask = Bukkit.getScheduler().runTaskLater(this, this::rebuildNow, ticks);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        requestDebouncedRebuild();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        requestDebouncedRebuild();
    }

    private void startPeriodicRefresh() {
        if (periodicTask != null) {
            periodicTask.cancel();
        }
        long ticks = settings.updates().periodicFullRefreshSeconds() * 20L;
        periodicTask = Bukkit.getScheduler().runTaskTimer(this, this::rebuildNow, ticks, ticks);
    }

    private void registerCommand() {
        PluginCommand command = Objects.requireNonNull(getCommand("bcn"), "bcn command missing from plugin.yml");
        BcnCommand executor = new BcnCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void warnConfigWarnings() {
        for (String warning : settings.warnings()) {
            getLogger().warning(warning);
        }
    }

    private void runOnMainThread(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(this, runnable);
        }
    }

    public Path webDir() {
        return getDataFolder().toPath().resolve(settings.web().localWebDir());
    }

    private Path playersJsonPath() {
        return webDir().resolve(settings.web().jsonFile());
    }

    public PluginSettings settings() {
        return settings;
    }

    public LuckPermsService luckPermsService() {
        return luckPermsService;
    }

    public FloodgateBridge floodgateBridge() {
        return floodgateBridge;
    }

    public PlayerSnapshotService snapshotService() {
        return snapshotService;
    }

    public JsonPublisher jsonPublisher() {
        return jsonPublisher;
    }

    public BlueMapRegistrar blueMapRegistrar() {
        return blueMapRegistrar;
    }
}
