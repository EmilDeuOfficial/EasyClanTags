package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class InputService {

    private record Pending(long token, Consumer<String> onText, Runnable onCancel) {
    }

    private final EasyClanTags plugin;
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();
    private final AtomicLong tokens = new AtomicLong();

    public InputService(EasyClanTags plugin) {
        this.plugin = plugin;
    }

    public boolean has(UUID player) {
        return pending.containsKey(player);
    }

    public void request(Player player, String promptKey, Consumer<String> onText, Runnable onCancel) {
        long token = tokens.incrementAndGet();
        pending.put(player.getUniqueId(), new Pending(token, onText, onCancel));
        plugin.lang().send(player, promptKey);
        plugin.lang().send(player, "input.hint");
        int ticks = plugin.settings().inputTimeoutSeconds * 20;
        UUID id = player.getUniqueId();
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            Pending current = pending.get(id);
            if (current != null && current.token() == token && pending.remove(id, current)) {
                Player online = plugin.getServer().getPlayer(id);
                if (online != null) {
                    online.getScheduler().run(plugin, inner -> {
                        plugin.lang().send(online, "input.timeout");
                        current.onCancel().run();
                    }, null);
                }
            }
        }, ticks);
    }

    public boolean consume(Player player, String text) {
        Pending entry = pending.remove(player.getUniqueId());
        if (entry == null) {
            return false;
        }
        boolean cancelled = text.equalsIgnoreCase("cancel");
        player.getScheduler().run(plugin, task -> {
            if (cancelled) {
                plugin.lang().send(player, "input.cancelled");
                entry.onCancel().run();
            } else {
                entry.onText().accept(text.trim());
            }
        }, null);
        return true;
    }

    public void drop(UUID player) {
        pending.remove(player);
    }

    public void clear() {
        pending.clear();
    }
}
