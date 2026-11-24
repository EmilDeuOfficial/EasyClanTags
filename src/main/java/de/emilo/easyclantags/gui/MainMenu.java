package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.clan.ClanManager;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.Result;
import de.emilo.easyclantags.tag.TagService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.UUID;

final class MainMenu extends Menu {

    MainMenu(EasyClanTags plugin, GuiManager gui, Player player) {
        super(plugin, gui, player);
    }

    @Override
    protected Component title() {
        return plugin.lang().component("gui.main.title");
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected void build() {
        ItemFactory items = gui.items();
        ClanManager clans = plugin.clans();
        UUID id = player.getUniqueId();
        ClanData clan = clans.clanOf(id).orElse(null);

        if (clan == null) {
            set(items.slot("info"), items.create("info", "-none"));
            if (player.hasPermission("easyclantags.create")) {
                set(items.slot("create-clan"), items.create("create-clan"), event -> createClan());
            }
            int invites = clans.pendingInvites(id).size();
            if (invites > 0 && player.hasPermission("easyclantags.accept")) {
                set(items.slot("invites"), items.create("invites", "", Placeholder.unparsed("count", String.valueOf(invites))),
                        event -> new InvitesMenu(plugin, gui, player).open());
            }
            return;
        }

        boolean owner = clan.owner().equals(id);
        TagResolver clanResolvers = gui.clanResolvers(clan);
        TagResolver role = Placeholder.component("role", plugin.lang().component(owner ? "gui.role.owner" : "gui.role.member"));
        set(items.slot("info"), items.create("info", "", clanResolvers, role));
        if (owner && player.hasPermission("easyclantags.edit")) {
            set(items.slot("edit-tag"), items.create("edit-tag", clan.tag().isEmpty() ? "-new" : "", clanResolvers), event -> editTag());
        }
        set(items.slot("members"), items.create("members", owner ? "-owner" : "", clanResolvers),
                event -> new MembersMenu(plugin, gui, player).open());
        set(items.slot("preview"), items.create("preview"), event -> {
            ClanData current = clans.clanOf(id).orElse(null);
            if (current != null) {
                gui.sendPreview(player, current.name(), current.tag());
            }
        });
        if (owner && player.hasPermission("easyclantags.delete")) {
            set(items.slot("disband"), items.create("disband", "", clanResolvers), event -> confirmDisband());
        } else if (!owner && player.hasPermission("easyclantags.leave")) {
            set(items.slot("leave"), items.create("leave", "", clanResolvers), event -> confirmLeave());
        }
    }

    private void createClan() {
        if (!allowed("easyclantags.create")) {
            return;
        }
        gui.prompt(player, "input.clan-name", text -> {
            plugin.lang().send(player, plugin.clans().create(player.getUniqueId(), player.getName(), text));
            gui.openMain(player);
        }, () -> gui.openMain(player));
    }

    private void editTag() {
        if (!allowed("easyclantags.edit")) {
            return;
        }
        ClanData clan = plugin.clans().clanOf(player.getUniqueId()).orElse(null);
        if (clan == null) {
            plugin.lang().send(player, "err.clan.none");
            return;
        }
        if (!clan.owner().equals(player.getUniqueId())) {
            plugin.lang().send(player, "err.clan.not-owner");
            return;
        }
        long remaining = plugin.clans().cooldownRemainingSeconds(clan);
        if (remaining > 0 && !player.hasPermission(TagService.PERM_BYPASS_COOLDOWN)) {
            plugin.lang().send(player, "err.cooldown", Placeholder.unparsed("time", ClanManager.formatSeconds(remaining)));
            return;
        }
        if (!clan.tag().isEmpty()) {
            gui.startEditor(player, clan.tag());
            return;
        }
        gui.prompt(player, "input.tag-text", text -> {
            Result result = plugin.tags().validateText(text, player::hasPermission);
            if (!result.ok()) {
                plugin.lang().send(player, result);
                gui.openMain(player);
                return;
            }
            gui.startEditor(player, ClanTag.EMPTY.withText(text, plugin.settings().defaultColor));
        }, () -> gui.openMain(player));
    }

    private void confirmLeave() {
        new ConfirmMenu(plugin, gui, player, plugin.lang().component("gui.confirm.leave.title"), () -> {
            plugin.lang().send(player, plugin.clans().leave(player.getUniqueId()));
            gui.openMain(player);
        }, () -> gui.openMain(player)).open();
    }

    private void confirmDisband() {
        new ConfirmMenu(plugin, gui, player, plugin.lang().component("gui.confirm.disband.title"), () -> {
            plugin.lang().send(player, plugin.clans().disband(player.getUniqueId()));
            gui.openMain(player);
        }, () -> gui.openMain(player)).open();
    }
}
