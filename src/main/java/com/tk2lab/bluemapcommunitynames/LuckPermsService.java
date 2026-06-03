package com.tk2lab.bluemapcommunitynames;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class LuckPermsService {
    private final JavaPlugin plugin;
    private LuckPerms luckPerms;
    private EventSubscription<UserDataRecalculateEvent> subscription;

    public LuckPermsService(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public boolean load() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        this.luckPerms = provider == null ? null : provider.getProvider();
        return this.luckPerms != null;
    }

    public String metaValue(Player player, String metaKey) {
        if (luckPerms == null) {
            return null;
        }
        return luckPerms.getPlayerAdapter(Player.class).getMetaData(player).getMetaValue(metaKey);
    }

    public void subscribe(Consumer<UUID> onlineUserRecalculated) {
        if (luckPerms == null || subscription != null) {
            return;
        }
        subscription = luckPerms.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, event -> {
            UUID uuid = event.getUser().getUniqueId();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (Bukkit.getPlayer(uuid) != null) {
                    onlineUserRecalculated.accept(uuid);
                }
            });
        });
    }

    public void close() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    public boolean detected() {
        return luckPerms != null;
    }
}
