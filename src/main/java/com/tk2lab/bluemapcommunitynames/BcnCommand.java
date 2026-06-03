package com.tk2lab.bluemapcommunitynames;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BcnCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("status", "rebuild", "reload");
    private final BlueMapCommunityNamesPlugin plugin;

    public BcnCommand(BlueMapCommunityNamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bluemapcommunitynames.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            sendStatus(sender);
            return true;
        }
        if ("rebuild".equalsIgnoreCase(args[0])) {
            plugin.rebuildNow();
            sender.sendMessage("BlueMapCommunityNames players.json rebuild scheduled.");
            return true;
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadPlugin();
            sender.sendMessage("BlueMapCommunityNames config reloaded and assets regenerated.");
            sender.sendMessage("If public path or script/style files changed, run /bluemap reload.");
            return true;
        }
        return false;
    }

    private void sendStatus(CommandSender sender) {
        PluginSettings settings = plugin.settings();
        Path webDir = plugin.webDir();
        Instant lastWrite = plugin.jsonPublisher().lastSuccessfulWrite();
        sender.sendMessage("BlueMapCommunityNames status:");
        sender.sendMessage("- BlueMap detected: " + yesNo(plugin.blueMapRegistrar().detected()));
        sender.sendMessage("- BlueMap version: " + plugin.blueMapRegistrar().blueMapVersion());
        sender.sendMessage("- BlueMap API version: " + plugin.blueMapRegistrar().apiVersion());
        sender.sendMessage("- LuckPerms detected: " + yesNo(plugin.luckPermsService().detected()));
        sender.sendMessage("- Floodgate detected: " + yesNo(plugin.floodgateBridge().available()));
        sender.sendMessage("- Bedrock detection mode: " + plugin.floodgateBridge().mode());
        sender.sendMessage("- Player roster enabled: " + yesNo(settings.roster().enabled()));
        sender.sendMessage("- Roster default state: " + settings.roster().panel().defaultState());
        sender.sendMessage("- Roster position: " + settings.roster().panel().position());
        sender.sendMessage("- Roster name mode: " + settings.roster().nameDisplay().mode());
        sender.sendMessage("- Roster details default state: " + settings.roster().details().defaultState());
        sender.sendMessage("- Roster details toggle allowed: " + yesNo(settings.roster().details().allowToggle()));
        sender.sendMessage("- Roster filters enabled: " + yesNo(settings.roster().filters().enabled()));
        sender.sendMessage("- Roster filter sections collapsed by default: " + yesNo(settings.roster().filters().fieldSectionsCollapsedByDefault()));
        sender.sendMessage("- Roster max visible filter values per field: " + settings.roster().filters().maxVisibleValuesPerField());
        sender.sendMessage("- Roster high-cardinality threshold: " + settings.roster().filters().highCardinalityThreshold());
        sender.sendMessage("- Active roster fields: " + settings.display().fields().size());
        if (settings.display().fields().isEmpty()) {
            sender.sendMessage("- No LuckPerms roster fields configured; roster will show Minecraft IDs only.");
        }
        Map<String, Integer> currentValueCounts = currentValueCounts(settings);
        for (PluginSettings.DisplayField field : settings.display().fields()) {
            sender.sendMessage("  - id=" + field.id()
                    + ", key=" + field.key()
                    + ", label=" + field.label()
                    + ", display=" + field.display()
                    + ", enabled=" + yesNo(field.enabled())
                    + ", searchable=" + yesNo(field.searchable())
                    + ", filterable=" + yesNo(field.filterable())
                    + ", current-values=" + currentValueCounts.getOrDefault(field.id(), 0));
        }
        sender.sendMessage("- Config fallback used: " + yesNo(settings.display().fallbackUsed()));
        if (!settings.warnings().isEmpty()) {
            sender.sendMessage("- Config warnings: " + settings.warnings().size());
            for (String warning : settings.warnings()) {
                sender.sendMessage("  - " + warning);
            }
        }
        sender.sendMessage("- Plugin data folder: " + plugin.getDataFolder().toPath().toAbsolutePath());
        sender.sendMessage("- Web assets folder: " + webDir.toAbsolutePath());
        sender.sendMessage("- Public base path: " + settings.web().normalizedPublicBasePath());
        sender.sendMessage("- Last successful JSON write: " + (lastWrite == null ? "never" : lastWrite));
        sender.sendMessage("- Last error: " + plugin.jsonPublisher().lastErrorSummary());
        sender.sendMessage("- Strict-clean mode: enabled");
        sender.sendMessage("- nginx alias is required for public /bcn/ access.");
        sender.sendMessage("- Suggested checks:");
        sender.sendMessage("  curl -I https://your-map-domain.example" + settings.web().normalizedPublicBasePath() + settings.web().scriptFile());
        sender.sendMessage("  curl -I https://your-map-domain.example" + settings.web().normalizedPublicBasePath() + settings.web().jsonFile());
    }

    private String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private Map<String, Integer> currentValueCounts(PluginSettings settings) {
        Map<String, Integer> counts = new HashMap<>();
        if (settings.display().fields().isEmpty()) {
            return counts;
        }
        PlayersDocument snapshot = plugin.snapshotService().snapshot(settings);
        for (PlayerEntry player : snapshot.players()) {
            for (ResolvedMetaValue metaValue : player.metaValues()) {
                counts.merge(metaValue.id(), 1, Integer::sum);
            }
        }
        return counts;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return SUBCOMMANDS.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        return List.of();
    }
}
