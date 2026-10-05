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
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class NordTabPlugin extends JavaPlugin implements Listener {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask updateTask;
    private String headerTemplate;
    private String footerTemplate;
    private boolean plainPlayerNames;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        reloadSettings();
        getLogger().info("NordTab enabled without packet or placeholder dependencies.");
    }

    @Override
    public void onDisable() {
        if (updateTask != null) updateTask.cancel();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            if (plainPlayerNames) player.playerListName(null);
        }
    }

    private void reloadSettings() {
        reloadConfig();
        boolean previouslyPlain = plainPlayerNames;
        headerTemplate = getConfig().getString("header", "<dark_green><bold>Minecraft server</bold></dark_green>");
        footerTemplate = getConfig().getString("footer", "<gray><tps> tps - <online> players online - <ping> ping</gray>");
        plainPlayerNames = getConfig().getBoolean("plain-player-names", true);
        if (previouslyPlain && !plainPlayerNames) {
            for (Player player : Bukkit.getOnlinePlayers()) player.playerListName(null);
        }
        long interval = Math.max(20L, getConfig().getLong("update-interval-ticks", 40L));
        if (updateTask != null) updateTask.cancel();
        updateTask = Bukkit.getScheduler().runTaskTimer(this, this::updateAll, 1L, interval);
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) update(player);
    }

    private void update(Player viewer) {
        double currentTps = Math.min(20.0D, Bukkit.getTPS()[0]);
        Component header = miniMessage.deserialize(headerTemplate);
        Component footer = miniMessage.deserialize(footerTemplate,
                Placeholder.unparsed("tps", String.format(Locale.ROOT, "%.1f", currentTps)),
                Placeholder.unparsed("online", Integer.toString(Bukkit.getOnlinePlayers().size())),
                Placeholder.unparsed("ping", Integer.toString(Math.max(0, viewer.getPing()))));
        viewer.sendPlayerListHeaderAndFooter(header, footer);
        if (plainPlayerNames) viewer.playerListName(Component.text(viewer.getName()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(this, () -> update(event.getPlayer()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadSettings();
            sender.sendMessage(Component.text("NordTab configuration reloaded."));
        } else {
            sender.sendMessage(Component.text("Usage: /nordtab reload"));
        }
        return true;
    }
}
