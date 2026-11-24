package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.model.ClanData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class MembersMenu extends Menu {

    MembersMenu(EasyClanTags plugin, GuiManager gui, Player player) {
        super(plugin, gui, player);
    }

    @Override
    protected Component title() {
        ClanData clan = plugin.clans().clanOf(player.getUniqueId()).orElse(null);
        return plugin.lang().component("gui.members.title", Placeholder.unparsed("clan", clan == null ? "-" : clan.name()));
    }

    @Override
    protected int rows() {
        return 6;
    }

    @Override
    protected void build() {
        ItemFactory items = gui.items();
        set(items.slot("back"), items.create("back"), event -> gui.openMain(player));
        UUID viewer = player.getUniqueId();
        ClanData clan = plugin.clans().clanOf(viewer).orElse(null);
        if (clan == null) {
            return;
        }
        boolean viewerIsOwner = clan.owner().equals(viewer);
        List<Map.Entry<UUID, String>> members = new ArrayList<>(clan.members().entrySet());
        members.sort(Comparator.<Map.Entry<UUID, String>, Boolean>comparing(entry -> !entry.getKey().equals(clan.owner()))
                .thenComparing(entry -> entry.getValue().toLowerCase(Locale.ROOT)));
        int slot = 0;
        for (Map.Entry<UUID, String> entry : members) {
            if (slot >= 45) {
                break;
            }
            UUID memberId = entry.getKey();
            boolean memberIsOwner = memberId.equals(clan.owner());
            String variant = memberIsOwner ? "-owner" : (viewerIsOwner ? "-manage" : "-member");
            OfflinePlayer offline = Bukkit.getOfflinePlayer(memberId);
            Component status = plugin.lang().component(offline.isOnline() ? "gui.status.online" : "gui.status.offline");
            ItemStack head = items.create("member", variant, Placeholder.unparsed("player", entry.getValue()),
                    Placeholder.component("status", status));
            if (head.getItemMeta() instanceof SkullMeta skull) {
                skull.setOwningPlayer(offline);
                head.setItemMeta(skull);
            }
            boolean manageable = viewerIsOwner && !memberIsOwner;
            set(slot++, head, manageable
                    ? event -> new MemberActionMenu(plugin, gui, player, memberId, entry.getValue()).open()
                    : null);
        }
        if (viewerIsOwner && clan.members().size() < plugin.settings().maxMembers && player.hasPermission("easyclantags.invite")) {
            set(items.slot("invite-player"), items.create("invite-player"), event -> new InvitePlayersMenu(plugin, gui, player).open());
        }
    }
}
