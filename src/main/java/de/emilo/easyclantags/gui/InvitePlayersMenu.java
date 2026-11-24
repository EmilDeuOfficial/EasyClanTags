package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class InvitePlayersMenu extends Menu {

    private static final int PAGE_SIZE = 45;

    private int page;

    InvitePlayersMenu(EasyClanTags plugin, GuiManager gui, Player player) {
        super(plugin, gui, player);
    }

    @Override
    protected Component title() {
        return plugin.lang().component("gui.invite.title");
    }

    @Override
    protected int rows() {
        return 6;
    }

    @Override
    protected void build() {
        ItemFactory items = gui.items();
        set(items.slot("back"), items.create("back"), event -> new MembersMenu(plugin, gui, player).open());
        List<Player> candidates = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && plugin.clans().clanOf(online.getUniqueId()).isEmpty()) {
                candidates.add(online);
            }
        }
        candidates.sort(Comparator.comparing(candidate -> candidate.getName().toLowerCase(Locale.ROOT)));
        int pages = Math.max(1, (candidates.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));
        int from = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && from + i < candidates.size(); i++) {
            Player candidate = candidates.get(from + i);
            ItemStack head = items.create("member", "-invite", Placeholder.unparsed("player", candidate.getName()));
            if (head.getItemMeta() instanceof SkullMeta skull) {
                skull.setOwningPlayer(candidate);
                head.setItemMeta(skull);
            }
            set(i, head, event -> {
                if (!allowed("easyclantags.invite")) {
                    return;
                }
                plugin.lang().send(player, plugin.clans().invite(player.getUniqueId(), candidate.getUniqueId(), candidate.getName()));
                redraw();
            });
        }
        if (page > 0) {
            set(items.slot("prev-page"), items.create("prev-page"), event -> {
                page--;
                redraw();
            });
        }
        if (page < pages - 1) {
            set(items.slot("next-page"), items.create("next-page"), event -> {
                page++;
                redraw();
            });
        }
    }
}
