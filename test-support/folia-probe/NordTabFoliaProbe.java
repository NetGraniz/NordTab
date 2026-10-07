package com.nordfjell.nordtab.probe;

import com.nordfjell.nordtab.NordTabPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** Synthetic player backed by a real entity scheduler, never installs on production. */
public final class NordTabFoliaProbe extends JavaPlugin {
    private final AtomicInteger updates = new AtomicInteger();
    private volatile boolean failed;

    @Override public void onEnable() {
        Bukkit.getGlobalRegionScheduler().runDelayed(this, ignored -> {
            var world = Bukkit.getWorlds().getFirst();
            var location = new Location(world, 8.5, 80.0, 8.5);
            world.getChunkAtAsync(0, 0, true).thenAccept(chunk ->
                    Bukkit.getRegionScheduler().execute(this, location, () -> runProbe(location)))
                    .exceptionally(error -> { getLogger().severe("NORDTAB_PROBE_FAIL chunk load: " + error); return null; });
        }, 20L);
    }

    private void runProbe(Location location) {
        location.getWorld().addPluginChunkTicket(0, 0, this);
        Entity owner = location.getWorld().spawnEntity(location, EntityType.PIG);
        owner.setGravity(false);
        UUID id = UUID.randomUUID();
        NordTabPlugin target = (NordTabPlugin) Bukkit.getPluginManager().getPlugin("NordTab");
        if (target == null || !target.isEnabled()) throw new IllegalStateException("NordTab is not enabled");
        Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(),
                new Class<?>[] {Player.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> id;
                    case "getName" -> "SyntheticTab";
                    case "getScheduler" -> owner.getScheduler();
                    case "getPing" -> 42;
                    case "isOnline", "isValid" -> true;
                    case "sendPlayerListHeaderAndFooter" -> {
                        if (!Bukkit.isOwnedByCurrentRegion(owner)) {
                            failed = true;
                            throw new IllegalStateException("Update ran outside the owning region");
                        }
                        String footer = PlainTextComponentSerializer.plainText().serialize((Component) args[1]);
                        if (!footer.contains("42 ping") || !footer.contains("tps")) failed = true;
                        updates.incrementAndGet();
                        yield null;
                    }
                    case "playerListName" -> null;
                    case "toString" -> "SyntheticTab";
                    case "hashCode" -> id.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        target.onJoin(new PlayerJoinEvent(player, (Component) null));
        owner.getScheduler().runDelayed(this, ignored -> {
            target.onQuit(new PlayerQuitEvent(player, (Component) null));
            int before = updates.get();
            owner.getScheduler().runDelayed(this, done -> {
                if (failed || before < 1 || updates.get() != before) {
                    getLogger().severe("NORDTAB_PROBE_FAIL updates=" + updates.get());
                } else {
                    getLogger().info("NORDTAB_PROBE_PASS entity-owned updates=" + before + "; quit cancels updates");
                }
                owner.remove();
                location.getWorld().removePluginChunkTicket(0, 0, this);
            }, null, 45L);
        }, null, 85L);
    }
}
