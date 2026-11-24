package de.emilo.easyclantags.listener;

import de.emilo.easyclantags.EasyClanTags;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerListener implements Listener {

    private final EasyClanTags plugin;

    public PlayerListener(EasyClanTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.clans().touchName(player.getUniqueId(), player.getName());
        player.getScheduler().runDelayed(plugin, task -> plugin.display().refreshAll(), null, 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.display().playerQuit(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, task -> plugin.display().refreshAll(), null, 1L);
    }
}
