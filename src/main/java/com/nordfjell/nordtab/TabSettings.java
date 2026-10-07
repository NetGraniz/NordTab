package com.nordfjell.nordtab;

import org.bukkit.configuration.file.FileConfiguration;

/** Published atomically so region threads never read a mutable Bukkit configuration. */
record TabSettings(String header, String footer, boolean plainPlayerNames, long interval) {
    static TabSettings from(FileConfiguration config) {
        return new TabSettings(
                config.getString("header", "<dark_green><bold>Minecraft server</bold></dark_green>"),
                config.getString("footer", "<gray><tps> tps - <online> players online - <ping> ping</gray>"),
                config.getBoolean("plain-player-names", true),
                Math.max(20L, config.getLong("update-interval-ticks", 40L)));
    }
}
