package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class GuiManager implements Listener {

    private final EasyClanTags plugin;
    private final ItemFactory items;
    private final Map<UUID, EditorSession> sessions = new ConcurrentHashMap<>();
    private final Set<UUID> prompting = ConcurrentHashMap.newKeySet();

    public GuiManager(EasyClanTags plugin) {
        this.plugin = plugin;
        this.items = new ItemFactory(plugin);
    }

    public ItemFactory items() {
        return items;
    }

    public void openMain(Player player) {
        new MainMenu(plugin, this, player).open();
    }

    public void prompt(Player player, String promptKey, Consumer<String> onText, Runnable onCancel) {
        UUID id = player.getUniqueId();
        prompting.add(id);
        try {
            player.closeInventory();
        } finally {
            prompting.remove(id);
        }
        plugin.input().request(player, promptKey, onText, onCancel);
    }

    void startEditor(Player player, ClanTag tag) {
        EditorSession session = new EditorSession(tag);
        sessions.put(player.getUniqueId(), session);
        new TagEditorMenu(plugin, this, player, session).open();
    }

    void reopenEditor(Player player) {
        EditorSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            openMain(player);
            return;
        }
        session.clampSelection();
        new TagEditorMenu(plugin, this, player, session).open();
    }

    void endSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    void editorClosed(Player player) {
        if (!prompting.contains(player.getUniqueId())) {
            sessions.remove(player.getUniqueId());
        }
    }

    public void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder(false) instanceof Menu) {
                player.closeInventory();
            }
        }
        sessions.clear();
        prompting.clear();
    }

    TagResolver previewResolvers(Player player, String clanName, ClanTag tag) {
        Settings s = plugin.settings();
        String name = player.getName();
        Component disabled = plugin.lang().component("gui.preview.disabled");
        Component nametag = s.nametagEnabled ? plugin.renderer().nametagFull(clanName, tag, name) : disabled;
        Component tab = s.tabEnabled ? plugin.renderer().tab(clanName, tag, name) : disabled;
        Component chat = s.chatEnabled
                ? plugin.renderer().chat(clanName, tag, name, Component.text(name), Component.text(plugin.lang().raw("gui.preview.sample")))
                : disabled;
        return TagResolver.resolver(
                Placeholder.component("nametag", nametag),
                Placeholder.component("tab", tab),
                Placeholder.component("chat", chat));
    }

    void sendPreview(Player player, String clanName, ClanTag tag) {
        TagResolver resolver = previewResolvers(player, clanName, tag);
        player.sendMessage(plugin.lang().component("preview.header"));
        player.sendMessage(plugin.lang().component("preview.nametag", resolver));
        player.sendMessage(plugin.lang().component("preview.tab", resolver));
        player.sendMessage(plugin.lang().component("preview.chat", resolver));
    }

    TagResolver clanResolvers(ClanData clan) {
        Component tag = clan.tag().isEmpty()
                ? plugin.lang().component("gui.none")
                : plugin.renderer().bracketed(clan.tag());
        return TagResolver.resolver(
                Placeholder.unparsed("clan", clan.name()),
                Placeholder.unparsed("owner", clan.members().getOrDefault(clan.owner(), "?")),
                Placeholder.unparsed("members", String.valueOf(clan.members().size())),
                Placeholder.unparsed("max", String.valueOf(plugin.settings().maxMembers)),
                Placeholder.component("tag", tag));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof Menu menu)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() != top) {
            return;
        }
        menu.handleClick(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof Menu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder(false) instanceof Menu menu) {
            menu.onClose();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
        prompting.remove(event.getPlayer().getUniqueId());
        plugin.input().drop(event.getPlayer().getUniqueId());
    }
}
