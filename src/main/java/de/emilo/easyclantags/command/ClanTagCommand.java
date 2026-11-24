package de.emilo.easyclantags.command;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.Result;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ClanTagCommand implements TabExecutor {

    private static final Map<String, String> SUBCOMMANDS = Map.ofEntries(
            Map.entry("on", "easyclantags.toggle"),
            Map.entry("off", "easyclantags.toggle"),
            Map.entry("status", "easyclantags.status"),
            Map.entry("reload", "easyclantags.reload"),
            Map.entry("create", "easyclantags.create"),
            Map.entry("delete", "easyclantags.delete"),
            Map.entry("invite", "easyclantags.invite"),
            Map.entry("accept", "easyclantags.accept"),
            Map.entry("deny", "easyclantags.accept"),
            Map.entry("leave", "easyclantags.leave"),
            Map.entry("kick", "easyclantags.kick"),
            Map.entry("transfer", "easyclantags.transfer"),
            Map.entry("info", "easyclantags.info"),
            Map.entry("list", "easyclantags.list"),
            Map.entry("admin", "easyclantags.admin"));

    private static final Map<String, String> ADMIN_SUBCOMMANDS = Map.of(
            "set", "easyclantags.admin.set",
            "remove", "easyclantags.admin.remove",
            "delete", "easyclantags.admin.delete");

    private static final Map<String, String> USAGE = Map.ofEntries(
            Map.entry("on", ""),
            Map.entry("off", ""),
            Map.entry("status", ""),
            Map.entry("reload", ""),
            Map.entry("create", "<name>"),
            Map.entry("delete", "confirm"),
            Map.entry("invite", "<player>"),
            Map.entry("accept", "[clan]"),
            Map.entry("deny", "[clan]"),
            Map.entry("leave", ""),
            Map.entry("kick", "<player>"),
            Map.entry("transfer", "<player>"),
            Map.entry("info", "[clan|tag]"),
            Map.entry("list", ""),
            Map.entry("admin", "<set|remove|delete> ..."));

    private static final List<String> ORDER =List.of("on", "off", "status", "reload", "create", "delete", "invite",
            "accept", "deny", "leave", "kick", "transfer", "info", "list", "admin");

    private final EasyClanTags plugin;

    public ClanTagCommand(EasyClanTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (!sender.hasPermission("easyclantags.use")) {
                    plugin.lang().send(sender, "err.no-permission");
                } else if (!plugin.isActive()) {
                    plugin.lang().send(sender, "err.disabled");
                } else {
                    plugin.gui().openMain(player);
                }
            } else {
                sendHelp(sender, label);
            }
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        String permission = SUBCOMMANDS.get(sub);
        if (permission == null) {
            sendHelp(sender, label);
            return true;
        }
        boolean adminBranch = sub.equals("admin");
        if (!adminBranch && !sender.hasPermission(permission)) {
            plugin.lang().send(sender, "err.no-permission");
            return true;
        }
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        switch (sub) {
            case "on" -> toggle(sender, true);
            case "off" -> toggle(sender, false);
            case "status" -> status(sender);
            case "reload" -> {
                plugin.reloadAll();
                plugin.lang().send(sender, "cmd.reloaded");
            }
            case "admin" -> admin(sender, rest, label);
            default -> playerCommand(sender, sub, rest, label);
        }
        return true;
    }

    private void toggle(CommandSender sender, boolean enabled) {
        plugin.setGlobalEnabled(enabled);
        plugin.lang().send(sender, enabled ? "cmd.on" : "cmd.off");
    }

    private void status(CommandSender sender) {
        TagResolver resolver = TagResolver.resolver(
                Placeholder.component("state", plugin.lang().component(plugin.isActive() ? "gui.state.on" : "gui.state.off")),
                Placeholder.unparsed("clans", String.valueOf(plugin.clans().clanCount())),
                Placeholder.unparsed("tagged", String.valueOf(plugin.clans().taggedPlayerCount())),
                Placeholder.unparsed("storage", plugin.storage().describe()),
                Placeholder.unparsed("language", plugin.lang().code()));
        for (Component line : plugin.lang().lines("cmd.status", resolver)) {
            sender.sendMessage(line);
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(plugin.lang().component("cmd.help.header"));
        for (String sub : ORDER) {
            if (canUse(sender, sub)) {
                sender.sendMessage(plugin.lang().component("cmd.help.line",
                        Placeholder.unparsed("label", label), Placeholder.unparsed("sub", sub),
                        Placeholder.unparsed("usage", USAGE.get(sub)),
                        Placeholder.unparsed("description", plugin.lang().raw("cmd.description." + sub))));
            }
        }
    }

    private boolean canUse(CommandSender sender, String sub) {
        if (sub.equals("admin")) {
            for (String permission : ADMIN_SUBCOMMANDS.values()) {
                if (sender.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }
        return sender.hasPermission(SUBCOMMANDS.get(sub));
    }

    private void playerCommand(CommandSender sender, String sub, String[] args, String label) {
        if (sub.equals("info") || sub.equals("list")) {
            if (sub.equals("info")) {
                info(sender, args);
            } else {
                list(sender);
            }
            return;
        }
        if (!(sender instanceof Player player)) {
            plugin.lang().send(sender, "err.player-only");
            return;
        }
        if (!plugin.isActive()) {
            plugin.lang().send(sender, "err.disabled");
            return;
        }
        UUID id = player.getUniqueId();
        switch (sub) {
            case "create" -> {
                if (args.length < 1) {
                    usage(sender, label, sub);
                    return;
                }
                plugin.lang().send(sender, plugin.clans().create(id, player.getName(), args[0]));
            }
            case "delete" -> {
                if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
                    plugin.lang().send(sender, "cmd.delete-confirm", Placeholder.unparsed("label", label));
                    return;
                }
                plugin.lang().send(sender, plugin.clans().disband(id));
            }
            case "invite" -> {
                if (args.length < 1) {
                    usage(sender, label, sub);
                    return;
                }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    plugin.lang().send(sender, "err.player-not-found", Placeholder.unparsed("player", args[0]));
                    return;
                }
                plugin.lang().send(sender, plugin.clans().invite(id, target.getUniqueId(), target.getName()));
            }
            case "accept" -> plugin.lang().send(sender,
                    plugin.clans().accept(id, player.getName(), args.length > 0 ? args[0] : null));
            case "deny" -> plugin.lang().send(sender, plugin.clans().deny(id, args.length > 0 ? args[0] : null));
            case "leave" -> plugin.lang().send(sender, plugin.clans().leave(id));
            case "kick", "transfer" -> {
                if (args.length < 1) {
                    usage(sender, label, sub);
                    return;
                }
                UUID target = memberByName(id, args[0]);
                if (target == null) {
                    plugin.lang().send(sender, "err.target.not-member", Placeholder.unparsed("player", args[0]));
                    return;
                }
                plugin.lang().send(sender, sub.equals("kick")
                        ? plugin.clans().kick(id, target)
                        : plugin.clans().transfer(id, target));
            }
            default -> sendHelp(sender, label);
        }
    }

    private void usage(CommandSender sender, String label, String sub) {
        plugin.lang().send(sender, "err.usage", Placeholder.unparsed("usage", "/" + label + " " + sub + " " + USAGE.get(sub)));
    }

    private UUID memberByName(UUID actor, String name) {
        ClanData clan = plugin.clans().clanOf(actor).orElse(null);
        if (clan == null) {
            return null;
        }
        for (Map.Entry<UUID, String> entry : clan.members().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void info(CommandSender sender, String[] args) {
        ClanData clan;
        if (args.length > 0) {
            clan = plugin.clans().byName(args[0]).or(() -> plugin.clans().byTag(args[0])).orElse(null);
            if (clan == null) {
                plugin.lang().send(sender, "err.clan.unknown", Placeholder.unparsed("clan", args[0]));
                return;
            }
        } else if (sender instanceof Player player) {
            clan = plugin.clans().clanOf(player.getUniqueId()).orElse(null);
            if (clan == null) {
                plugin.lang().send(sender, "err.clan.none");
                return;
            }
        } else {
            plugin.lang().send(sender, "err.player-only");
            return;
        }
        Component tag = clan.tag().isEmpty() ? plugin.lang().component("gui.none") : plugin.renderer().bracketed(clan.tag());
        TagResolver resolver = TagResolver.resolver(
                Placeholder.unparsed("clan", clan.name()),
                Placeholder.unparsed("owner", clan.members().getOrDefault(clan.owner(), "?")),
                Placeholder.unparsed("members", String.valueOf(clan.members().size())),
                Placeholder.unparsed("max", String.valueOf(plugin.settings().maxMembers)),
                Placeholder.unparsed("names", String.join(", ", clan.members().values())),
                Placeholder.component("tag", tag));
        for (Component line : plugin.lang().lines("cmd.info", resolver)) {
            sender.sendMessage(line);
        }
    }

    private void list(CommandSender sender) {
        List<ClanData> clans = plugin.clans().allSorted();
        plugin.lang().send(sender, "cmd.list.header", Placeholder.unparsed("count", String.valueOf(clans.size())));
        for (ClanData clan : clans) {
            Component tag = clan.tag().isEmpty() ? plugin.lang().component("gui.none") : plugin.renderer().bracketed(clan.tag());
            sender.sendMessage(plugin.lang().component("cmd.list.entry",
                    Placeholder.unparsed("clan", clan.name()),
                    Placeholder.unparsed("members", String.valueOf(clan.members().size())),
                    Placeholder.component("tag", tag)));
        }
    }

    private void admin(CommandSender sender, String[] args, String label) {
        if (!canUse(sender, "admin")) {
            plugin.lang().send(sender, "err.no-permission");
            return;
        }
        if (args.length == 0 || !ADMIN_SUBCOMMANDS.containsKey(args[0].toLowerCase(Locale.ROOT))) {
            sendAdminHelp(sender, label);
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (!sender.hasPermission(ADMIN_SUBCOMMANDS.get(sub))) {
            plugin.lang().send(sender, "err.no-permission");
            return;
        }
        switch (sub) {
            case "set" -> {
                if (args.length < 3) {
                    plugin.lang().send(sender, "err.usage", Placeholder.unparsed("usage", "/" + label + " admin set <player> <tag>"));
                    return;
                }
                adminSet(sender, args[1], args[2]);
            }
            case "remove" -> {
                if (args.length < 2) {
                    plugin.lang().send(sender, "err.usage", Placeholder.unparsed("usage", "/" + label + " admin remove <player>"));
                    return;
                }
                Optional<UUID> target = resolvePlayer(args[1]);
                if (target.isEmpty()) {
                    plugin.lang().send(sender, "err.player-not-found", Placeholder.unparsed("player", args[1]));
                    return;
                }
                plugin.lang().send(sender, plugin.clans().adminRemoveTag(target.get()));
            }
            case "delete" -> {
                if (args.length < 2) {
                    plugin.lang().send(sender, "err.usage", Placeholder.unparsed("usage", "/" + label + " admin delete <tag>"));
                    return;
                }
                plugin.lang().send(sender, plugin.clans().adminDeleteByTag(args[1]));
            }
            default -> sendAdminHelp(sender, label);
        }
    }

    private void adminSet(CommandSender sender, String playerName, String text) {
        Optional<UUID> target = resolvePlayer(playerName);
        if (target.isEmpty()) {
            plugin.lang().send(sender, "err.player-not-found", Placeholder.unparsed("player", playerName));
            return;
        }
        ClanTag tag = ClanTag.EMPTY.withText(text, plugin.settings().defaultColor);
        UUID ownClan = plugin.clans().clanOf(target.get()).map(ClanData::id).orElse(null);
        Result valid = plugin.tags().validate(tag, ownClan, permission -> true);
        if (!valid.ok()) {
            plugin.lang().send(sender, valid);
            return;
        }
        String exactName = Optional.ofNullable(Bukkit.getOfflinePlayer(target.get()).getName()).orElse(playerName);
        plugin.lang().send(sender, plugin.clans().adminAssign(target.get(), exactName, tag));
    }

    private void sendAdminHelp(CommandSender sender, String label) {
        plugin.lang().send(sender, "err.usage", Placeholder.unparsed("usage",
                "/" + label + " admin <set <player> <tag> | remove <player> | delete <tag>>"));
    }

    private Optional<UUID> resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return Optional.of(online.getUniqueId());
        }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        return cached == null ? Optional.empty() : Optional.of(cached.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : ORDER) {
                if (canUse(sender, sub)) {
                    options.add(sub);
                }
            }
            return filter(options, args[0]);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        UUID id = sender instanceof Player player ? player.getUniqueId() : null;
        if (args.length == 2) {
            switch (sub) {
                case "invite" -> {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.getUniqueId().equals(id) && plugin.clans().clanOf(online.getUniqueId()).isEmpty()) {
                            options.add(online.getName());
                        }
                    }
                }
                case "kick", "transfer" -> plugin.clans().clanOf(id == null ? new UUID(0, 0) : id).ifPresent(clan -> {
                    for (Map.Entry<UUID, String> member : clan.members().entrySet()) {
                        if (!member.getKey().equals(clan.owner())) {
                            options.add(member.getValue());
                        }
                    }
                });
                case "accept", "deny" -> {
                    if (id != null) {
                        for (ClanData clan : plugin.clans().pendingInvites(id)) {
                            options.add(clan.name());
                        }
                    }
                }
                case "info" -> {
                    for (ClanData clan : plugin.clans().snapshot()) {
                        options.add(clan.name());
                    }
                }
                case "delete" -> options.add("confirm");
                case "admin" -> {
                    for (Map.Entry<String, String> entry : ADMIN_SUBCOMMANDS.entrySet()) {
                        if (sender.hasPermission(entry.getValue())) {
                            options.add(entry.getKey());
                        }
                    }
                }
                default -> {
                }
            }
            return filter(options, args[1]);
        }
        if (sub.equals("admin")) {
            String adminSub = args[1].toLowerCase(Locale.ROOT);
            if (args.length == 3 && (adminSub.equals("set") || adminSub.equals("remove"))) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    options.add(online.getName());
                }
                return filter(options, args[2]);
            }
            if (args.length == 3 && adminSub.equals("delete")) {
                for (ClanData clan : plugin.clans().snapshot()) {
                    if (!clan.tag().isEmpty()) {
                        options.add(clan.tag().plain());
                    }
                }
                return filter(options, args[2]);
            }
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
