package com.nordfjell.nordtab;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TabSettingsTest {
    @Test void defaultsRemainCompatible() {
        TabSettings settings = TabSettings.from(new YamlConfiguration());
        assertEquals(40L, settings.interval());
        assertTrue(settings.plainPlayerNames());
        assertTrue(settings.footer().contains("<online>"));
    }

    @Test void intervalsCannotFloodTheScheduler() {
        YamlConfiguration config = new YamlConfiguration();
        for (long interval : new long[] {Long.MIN_VALUE, -1, 0, 1, 19, 20, 40, 100}) {
            config.set("update-interval-ticks", interval);
            assertEquals(Math.max(20L, interval), TabSettings.from(config).interval());
        }
    }

    @Test void snapshotsDoNotChangeWhenConfigIsReloaded() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("header", "<green>Тест</green>");
        config.set("plain-player-names", true);
        TabSettings old = TabSettings.from(config);
        config.set("header", "Changed");
        config.set("plain-player-names", false);
        assertEquals("<green>Тест</green>", old.header());
        assertTrue(old.plainPlayerNames());
        assertEquals("Changed", TabSettings.from(config).header());
        assertFalse(TabSettings.from(config).plainPlayerNames());
    }
}
