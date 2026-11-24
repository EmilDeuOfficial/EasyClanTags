package de.emilo.easyclantags.listener;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {

    private static final String PERM_CHAT_COLORS = "easyclantags.chat.colors";

    private final EasyClanTags plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatListener(EasyClanTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInput(AsyncChatEvent event) {
        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (plugin.input().has(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            plugin.input().consume(event.getPlayer(), text);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Settings settings = plugin.settings();
        if (!plugin.isActive() || !settings.chatEnabled) {
            return;
        }
        Player sender = event.getPlayer();
        ClanData clan = plugin.clans().clanOf(sender.getUniqueId()).orElse(null);
        ClanTag tag = clan == null ? ClanTag.EMPTY : clan.tag();
        String clanName = clan == null ? null : clan.name();
        boolean formatted = settings.allowPlayerFormatting && sender.hasPermission(PERM_CHAT_COLORS);
        event.renderer(ChatRenderer.viewerUnaware((source, displayName, message) -> {
            Component body = formatted
                    ? miniMessage.deserialize(PlainTextComponentSerializer.plainText().serialize(message))
                    : message;
            return plugin.renderer().chat(clanName, tag, source.getName(), displayName, body);
        }));
    }
}
