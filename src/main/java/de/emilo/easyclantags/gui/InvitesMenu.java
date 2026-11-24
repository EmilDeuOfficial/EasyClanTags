package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.Result;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

final class InvitesMenu extends Menu {

    InvitesMenu(EasyClanTags plugin, GuiManager gui, Player player) {
        super(plugin, gui, player);
    }

    @Override
    protected Component title() {
        return plugin.lang().component("gui.invites.title");
    }

    @Override
    protected int rows() {
        return 6;
    }

    @Override
    protected void build() {
        ItemFactory items = gui.items();
        set(items.slot("back"), items.create("back"), event -> gui.openMain(player));
        List<ClanData> invites = plugin.clans().pendingInvites(player.getUniqueId());
        for (int i = 0; i < invites.size() && i < 45; i++) {
            ClanData clan = invites.get(i);
            set(i, items.create("invite-entry", "", gui.clanResolvers(clan)), event -> {
                if (!allowed("easyclantags.accept")) {
                    return;
                }
                Result result = event.isRightClick()
                        ? plugin.clans().deny(player.getUniqueId(), clan.name())
                        : plugin.clans().accept(player.getUniqueId(), player.getName(), clan.name());
                plugin.lang().send(player, result);
                if (result.ok() && !event.isRightClick()) {
                    gui.openMain(player);
                } else {
                    redraw();
                }
            });
        }
    }
}
