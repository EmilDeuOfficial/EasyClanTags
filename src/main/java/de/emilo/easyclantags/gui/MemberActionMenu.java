package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

final class MemberActionMenu extends Menu {

    private final UUID target;
    private final String targetName;

    MemberActionMenu(EasyClanTags plugin, GuiManager gui, Player player, UUID target, String targetName) {
        super(plugin, gui, player);
        this.target = target;
        this.targetName = targetName;
    }

    @Override
    protected Component title() {
        return plugin.lang().component("gui.member-action.title", Placeholder.unparsed("player", targetName));
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected void build() {
        ItemFactory items = gui.items();
        TagResolver playerResolver = Placeholder.unparsed("player", targetName);
        ItemStack head = items.create("member", "-action", playerResolver,
                Placeholder.component("status", plugin.lang().component(Bukkit.getOfflinePlayer(target).isOnline()
                        ? "gui.status.online" : "gui.status.offline")));
        if (head.getItemMeta() instanceof SkullMeta skull) {
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(target));
            head.setItemMeta(skull);
        }
        set(13, head);
        if (player.hasPermission("easyclantags.kick")) {
            set(items.slot("kick"), items.create("kick", "", playerResolver), event -> {
                plugin.lang().send(player, plugin.clans().kick(player.getUniqueId(), target));
                new MembersMenu(plugin, gui, player).open();
            });
        }
        if (player.hasPermission("easyclantags.transfer")) {
            set(items.slot("transfer"), items.create("transfer", "", playerResolver), event ->
                    new ConfirmMenu(plugin, gui, player,
                            plugin.lang().component("gui.confirm.transfer.title", playerResolver), () -> {
                        plugin.lang().send(player, plugin.clans().transfer(player.getUniqueId(), target));
                        gui.openMain(player);
                    }, () -> new MembersMenu(plugin, gui, player).open()).open());
        }
        set(items.slot("back-small"), items.create("back-small"), event -> new MembersMenu(plugin, gui, player).open());
    }
}
