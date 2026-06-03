package com.tk2lab.bluemapcommunitynames;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerSnapshotService {
    private final LuckPermsService luckPermsService;
    private final FloodgateBridge floodgateBridge;
    private final Clock clock;
    private final String pluginVersion;

    public PlayerSnapshotService(
            LuckPermsService luckPermsService,
            FloodgateBridge floodgateBridge,
            Clock clock,
            String pluginVersion
    ) {
        this.luckPermsService = luckPermsService;
        this.floodgateBridge = floodgateBridge;
        this.clock = clock;
        this.pluginVersion = pluginVersion;
    }

    public PlayersDocument snapshot(PluginSettings settings) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Player snapshots must be built on the main server thread");
        }

        List<PlayerEntry> players = Bukkit.getOnlinePlayers().stream()
                .map(player -> entry(player, settings))
                .sorted(Comparator.comparing(PlayerEntry::playerName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PlayerEntry::playerName))
                .toList();

        return PlayersDocument.create(
                Instant.now(clock).toString(),
                pluginVersion,
                floodgateBridge.mode(),
                settings.display(),
                settings.roster().details(),
                settings.roster().filters(),
                players
        );
    }

    private PlayerEntry entry(Player player, PluginSettings settings) {
        String playerName = player.getName();
        return MetaDisplayResolver.resolve(
                playerName,
                settings.display(),
                key -> luckPermsService.metaValue(player, key),
                settings.sanitize(),
                floodgateBridge.bedrock(player.getUniqueId())
        );
    }
}
