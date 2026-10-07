package com.nordfjell.nordtab;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerQuitEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NordTabPlugin extends JavaPlugin implements Listener {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, ScheduledTask> updateTasks = new ConcurrentHashMap<>();
    private final Set<UUID> ownedNames = ConcurrentHashMap.newKeySet();
    private volatile TabSettings settings;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        loadSettings();
        Bukkit.getGlobalRegionScheduler().execute(this, this::restartUpdates);
        getLogger().info("NordTab enabled without packet or placeholder dependencies.");
    }

    @Override
    public void onDisable() {
        updateTasks.values().forEach(ScheduledTask::cancel);
        updateTasks.clear();
        ownedNames.clear();
        // The plugin is already disabled here: scheduling entity cleanup is forbidden.
        // Do not access players from the shutdown/global thread on Folia.
    }

    private void loadSettings() {
        reloadConfig();
        settings = TabSettings.from(getConfig());
    }

    private void restartUpdates() {
        updateTasks.values().forEach(ScheduledTask::cancel);
        updateTasks.clear();
        for (Player player : Bukkit.getOnlinePlayers()) startUpdates(player);
    }

    private void startUpdates(Player player) {
        UUID id = player.getUniqueId();
        ScheduledTask previous = updateTasks.remove(id);
        if (previous != null) previous.cancel();
        ScheduledTask task = player.getScheduler().runAtFixedRate(this,
                ignored -> update(player), () -> {
                    updateTasks.remove(id);
                    ownedNames.remove(id);
                }, 1L, settings.interval());
        if (task != null) updateTasks.put(id, task);
    }

    private void update(Player viewer) {
        TabSettings current = settings;
        // On Folia getTPS() reports the current region; on Paper it reports server TPS.
        double currentTps = Math.min(20.0D, Bukkit.getTPS()[0]);
        Component header = miniMessage.deserialize(current.header());
        Component footer = miniMessage.deserialize(current.footer(),
                Placeholder.unparsed("tps", String.format(Locale.ROOT, "%.1f", currentTps)),
                Placeholder.unparsed("online", Integer.toString(Bukkit.getOnlinePlayers().size())),
                Placeholder.unparsed("ping", Integer.toString(Math.max(0, viewer.getPing()))));
        viewer.sendPlayerListHeaderAndFooter(header, footer);
        if (current.plainPlayerNames()) {
            viewer.playerListName(Component.text(viewer.getName()));
            ownedNames.add(viewer.getUniqueId());
        } else if (ownedNames.remove(viewer.getUniqueId())) {
            viewer.playerListName(null);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getGlobalRegionScheduler().execute(this, () -> startUpdates(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        ScheduledTask task = updateTasks.remove(id);
        if (task != null) task.cancel();
        ownedNames.remove(id);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            Bukkit.getGlobalRegionScheduler().execute(this, () -> {
                loadSettings();
                restartUpdates();
                if (sender instanceof Player player) {
                    player.getScheduler().execute(this,
                            () -> player.sendMessage(Component.text("NordTab configuration reloaded.")), null, 1L);
                } else {
                    sender.sendMessage(Component.text("NordTab configuration reloaded."));
                }
            });
        } else {
            sender.sendMessage(Component.text("Usage: /nordtab reload"));
        }
        return true;
    }
}
