package de.emilo.easyclantags.clan;

import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ClanManager {

    public interface Hooks {
        void changed(Collection<UUID> players);

        void notifyPlayer(UUID player, Result message);

        void saveRequested();
    }

    private static final class Effects {
        private final Set<UUID> changed = new HashSet<>();
        private final List<Map.Entry<UUID, Result>> notes = new ArrayList<>();
        private boolean save;

        void changed(UUID... players) {
            changed.addAll(List.of(players));
        }

        void changed(Collection<UUID> players) {
            changed.addAll(players);
        }

        void note(UUID player, Result result) {
            notes.add(Map.entry(player, result));
        }

        void save() {
            save = true;
        }
    }

    private final Object lock = new Object();
    private final Map<UUID, ClanData> clans = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerClan = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> invites = new ConcurrentHashMap<>();
    private final Supplier<Settings> settings;
    private volatile Hooks hooks = new Hooks() {
        @Override
        public void changed(Collection<UUID> players) {
        }

        @Override
        public void notifyPlayer(UUID player, Result message) {
        }

        @Override
        public void saveRequested() {
        }
    };

    public ClanManager(Supplier<Settings> settings) {
        this.settings = settings;
    }

    public void setHooks(Hooks hooks) {
        this.hooks = hooks;
    }

    public void load(Collection<ClanData> loaded) {
        synchronized (lock) {
            clans.clear();
            playerClan.clear();
            for (ClanData clan : loaded) {
                store(clan);
            }
        }
    }

    public List<ClanData> snapshot() {
        return new ArrayList<>(clans.values());
    }

    public List<ClanData> allSorted() {
        List<ClanData> list = snapshot();
        list.sort(Comparator.comparing(clan -> clan.name().toLowerCase(Locale.ROOT)));
        return list;
    }

    public Optional<ClanData> clanOf(UUID player) {
        UUID id = playerClan.get(player);
        return id == null ? Optional.empty() : Optional.ofNullable(clans.get(id));
    }

    public Optional<ClanData> byId(UUID id) {
        return Optional.ofNullable(clans.get(id));
    }

    public Optional<ClanData> byName(String name) {
        for (ClanData clan : clans.values()) {
            if (clan.name().equalsIgnoreCase(name)) {
                return Optional.of(clan);
            }
        }
        return Optional.empty();
    }

    public Optional<ClanData> byTag(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        for (ClanData clan : clans.values()) {
            if (!clan.tag().isEmpty() && clan.tag().normalized().equals(normalized)) {
                return Optional.of(clan);
            }
        }
        return Optional.empty();
    }

    public int clanCount() {
        return clans.size();
    }

    public int taggedPlayerCount() {
        int count = 0;
        for (ClanData clan : clans.values()) {
            if (!clan.tag().isEmpty()) {
                count += clan.members().size();
            }
        }
        return count;
    }

    public long cooldownRemainingSeconds(ClanData clan) {
        int cooldown = settings.get().changeCooldownSeconds;
        if (cooldown <= 0 || clan.tagChangedAt() <= 0) {
            return 0;
        }
        long remaining = clan.tagChangedAt() + cooldown * 1000L - System.currentTimeMillis();
        return remaining <= 0 ? 0 : (remaining + 999) / 1000;
    }

    public void touchName(UUID player, String name) {
        run(fx -> {
            ClanData clan = clanOf(player).orElse(null);
            if (clan != null && !name.equals(clan.members().get(player))) {
                clans.put(clan.id(), clan.withMember(player, name));
                fx.save();
            }
            return Result.ok("ok.touch");
        });
    }

    public Result create(UUID owner, String ownerName, String name) {
        return run(fx -> {
            if (playerClan.containsKey(owner)) {
                return Result.error("err.clan.already");
            }
            Settings s = settings.get();
            if (name.length() < s.minNameLength || name.length() > s.maxNameLength || !s.namePattern.matcher(name).matches()) {
                return Result.error("err.clan.name-invalid", "min", String.valueOf(s.minNameLength),
                        "max", String.valueOf(s.maxNameLength));
            }
            if (nameTaken(name)) {
                return Result.error("err.clan.name-taken", "clan", name);
            }
            Map<UUID, String> members = new LinkedHashMap<>();
            members.put(owner, ownerName);
            store(new ClanData(UUID.randomUUID(), name, owner, members, ClanTag.EMPTY, 0L, System.currentTimeMillis()));
            fx.changed(owner);
            fx.save();
            return Result.ok("ok.clan.created", "clan", name);
        });
    }

    public Result disband(UUID actor) {
        return run(fx -> {
            ClanData clan = clanOf(actor).orElse(null);
            if (clan == null) {
                return Result.error("err.clan.none");
            }
            if (!clan.owner().equals(actor)) {
                return Result.error("err.clan.not-owner");
            }
            remove(clan, fx, actor);
            return Result.ok("ok.clan.disbanded", "clan", clan.name());
        });
    }

    public Result invite(UUID actor, UUID target, String targetName) {
        return run(fx -> {
            ClanData clan = clanOf(actor).orElse(null);
            if (clan == null) {
                return Result.error("err.clan.none");
            }
            if (!clan.owner().equals(actor)) {
                return Result.error("err.clan.not-owner");
            }
            if (actor.equals(target)) {
                return Result.error("err.target.self");
            }
            if (playerClan.containsKey(target)) {
                return Result.error("err.target.in-clan", "player", targetName);
            }
            Settings s = settings.get();
            if (clan.members().size() >= s.maxMembers) {
                return Result.error("err.clan.full", "max", String.valueOf(s.maxMembers));
            }
            invites.computeIfAbsent(target, key -> new ConcurrentHashMap<>())
                    .put(clan.id(), System.currentTimeMillis() + s.inviteTimeoutSeconds * 1000L);
            fx.note(target, Result.ok("note.invited", "clan", clan.name(),
                    "player", clan.members().getOrDefault(actor, "?"), "seconds", String.valueOf(s.inviteTimeoutSeconds)));
            return Result.ok("ok.invite.sent", "player", targetName, "clan", clan.name());
        });
    }

    public List<ClanData> pendingInvites(UUID player) {
        Map<UUID, Long> map = invites.get(player);
        List<ClanData> result = new ArrayList<>();
        if (map == null) {
            return result;
        }
        long now = System.currentTimeMillis();
        map.entrySet().removeIf(entry -> entry.getValue() < now || !clans.containsKey(entry.getKey()));
        for (UUID clanId : map.keySet()) {
            ClanData clan = clans.get(clanId);
            if (clan != null) {
                result.add(clan);
            }
        }
        result.sort(Comparator.comparing(clan -> clan.name().toLowerCase(Locale.ROOT)));
        return result;
    }

    public Result accept(UUID player, String playerName, String clanName) {
        return run(fx -> {
            if (playerClan.containsKey(player)) {
                return Result.error("err.clan.already");
            }
            List<ClanData> candidates = pendingInvites(player);
            if (clanName != null) {
                candidates.removeIf(clan -> !clan.name().equalsIgnoreCase(clanName));
            }
            if (candidates.isEmpty()) {
                return Result.error("err.invite.none");
            }
            if (candidates.size() > 1) {
                return Result.error("err.invite.choose");
            }
            ClanData clan = candidates.get(0);
            Settings s = settings.get();
            if (clan.members().size() >= s.maxMembers) {
                return Result.error("err.clan.full", "max", String.valueOf(s.maxMembers));
            }
            ClanData updated = clan.withMember(player, playerName);
            store(updated);
            invites.remove(player);
            for (UUID member : clan.members().keySet()) {
                fx.note(member, Result.ok("note.joined", "player", playerName, "clan", clan.name()));
            }
            fx.changed(player);
            fx.save();
            return Result.ok("ok.invite.accepted", "clan", clan.name());
        });
    }

    public Result deny(UUID player, String clanName) {
        return run(fx -> {
            List<ClanData> candidates = pendingInvites(player);
            if (clanName != null) {
                candidates.removeIf(clan -> !clan.name().equalsIgnoreCase(clanName));
            }
            if (candidates.isEmpty()) {
                return Result.error("err.invite.none");
            }
            if (candidates.size() > 1) {
                return Result.error("err.invite.choose");
            }
            ClanData clan = candidates.get(0);
            Map<UUID, Long> map = invites.get(player);
            if (map != null) {
                map.remove(clan.id());
            }
            return Result.ok("ok.invite.denied", "clan", clan.name());
        });
    }

    public Result leave(UUID player) {
        return run(fx -> {
            ClanData clan = clanOf(player).orElse(null);
            if (clan == null) {
                return Result.error("err.clan.none");
            }
            if (clan.owner().equals(player)) {
                return Result.error("err.clan.owner-cannot-leave");
            }
            String name = clan.members().getOrDefault(player, "?");
            playerClan.remove(player);
            clans.put(clan.id(), clan.withoutMember(player));
            for (UUID member : clan.members().keySet()) {
                if (!member.equals(player)) {
                    fx.note(member, Result.ok("note.left", "player", name, "clan", clan.name()));
                }
            }
            fx.changed(player);
            fx.save();
            return Result.ok("ok.clan.left", "clan", clan.name());
        });
    }

    public Result kick(UUID actor, UUID target) {
        return run(fx -> {
            ClanData clan = clanOf(actor).orElse(null);
            if (clan == null) {
                return Result.error("err.clan.none");
            }
            if (!clan.owner().equals(actor)) {
                return Result.error("err.clan.not-owner");
            }
            if (actor.equals(target)) {
                return Result.error("err.target.self");
            }
            if (!clan.isMember(target)) {
                return Result.error("err.target.not-member", "player", "?");
            }
            String name = clan.members().get(target);
            playerClan.remove(target);
            clans.put(clan.id(), clan.withoutMember(target));
            fx.note(target, Result.ok("note.kicked", "clan", clan.name()));
            fx.changed(target);
            fx.save();
            return Result.ok("ok.member.kicked", "player", name);
        });
    }

    public Result transfer(UUID actor, UUID target) {
        return run(fx -> {
            ClanData clan = clanOf(actor).orElse(null);
            if (clan == null) {
                return Result.error("err.clan.none");
            }
            if (!clan.owner().equals(actor)) {
                return Result.error("err.clan.not-owner");
            }
            if (actor.equals(target)) {
                return Result.error("err.target.self");
            }
            if (!clan.isMember(target)) {
                return Result.error("err.target.not-member", "player", "?");
            }
            String name = clan.members().get(target);
            clans.put(clan.id(), clan.withOwner(target));
            for (UUID member : clan.members().keySet()) {
                if (!member.equals(actor)) {
                    fx.note(member, Result.ok("note.transferred", "player", name, "clan", clan.name()));
                }
            }
            fx.save();
            return Result.ok("ok.owner.transferred", "player", name);
        });
    }

    public Result setTag(UUID actor, ClanTag tag, boolean ignoreCooldown) {
        return run(fx -> {
            ClanData clan = clanOf(actor).orElse(null);
            if (clan == null) {
                return Result.error("err.clan.none");
            }
            if (!clan.owner().equals(actor)) {
                return Result.error("err.clan.not-owner");
            }
            if (!ignoreCooldown) {
                long remaining = cooldownRemainingSeconds(clan);
                if (remaining > 0) {
                    return Result.error("err.cooldown", "time", formatSeconds(remaining));
                }
            }
            return applyTag(clan, tag, fx);
        });
    }

    public Result adminAssign(UUID player, String playerName, ClanTag tag) {
        return run(fx -> {
            ClanData clan = clanOf(player).orElse(null);
            ClanData holder = byTag(tag.plain()).orElse(null);
            if (holder != null && (clan == null || !holder.id().equals(clan.id()))) {
                return Result.error("err.tag.taken", "tag", tag.plain(), "clan", holder.name());
            }
            if (clan == null) {
                Map<UUID, String> members = new LinkedHashMap<>();
                members.put(player, playerName);
                clan = new ClanData(UUID.randomUUID(), uniqueName(tag.plain()), player, members, ClanTag.EMPTY, 0L,
                        System.currentTimeMillis());
                store(clan);
            }
            return applyTag(clan, tag, fx);
        });
    }

    public Result adminRemoveTag(UUID player) {
        return run(fx -> {
            ClanData clan = clanOf(player).orElse(null);
            if (clan == null) {
                return Result.error("err.clan.none-other");
            }
            if (clan.tag().isEmpty()) {
                return Result.error("err.tag.none");
            }
            clans.put(clan.id(), clan.withTag(ClanTag.EMPTY, System.currentTimeMillis()));
            fx.changed(clan.members().keySet());
            fx.save();
            return Result.ok("ok.admin.removed", "clan", clan.name());
        });
    }

    public Result adminDeleteByTag(String text) {
        return run(fx -> {
            ClanData clan = byTag(text).orElse(null);
            if (clan == null) {
                return Result.error("err.tag.unknown", "tag", text);
            }
            remove(clan, fx, null);
            return Result.ok("ok.admin.deleted", "clan", clan.name(), "tag", clan.tag().plain());
        });
    }

    private Result applyTag(ClanData clan, ClanTag tag, Effects fx) {
        if (!tag.isEmpty()) {
            ClanData holder = byTag(tag.plain()).orElse(null);
            if (holder != null && !holder.id().equals(clan.id())) {
                return Result.error("err.tag.taken", "tag", tag.plain(), "clan", holder.name());
            }
        }
        clans.put(clan.id(), clan.withTag(tag, System.currentTimeMillis()));
        fx.changed(clan.members().keySet());
        fx.save();
        return Result.ok("ok.tag.set", "tag", tag.plain());
    }

    private void remove(ClanData clan, Effects fx, UUID actor) {
        clans.remove(clan.id());
        for (UUID member : clan.members().keySet()) {
            playerClan.remove(member);
            if (!member.equals(actor)) {
                fx.note(member, Result.ok("note.disbanded", "clan", clan.name()));
            }
        }
        fx.changed(clan.members().keySet());
        fx.save();
    }

    private void store(ClanData clan) {
        clans.put(clan.id(), clan);
        for (UUID member : clan.members().keySet()) {
            playerClan.put(member, clan.id());
        }
    }

    private boolean nameTaken(String name) {
        return byName(name).isPresent();
    }

    private String uniqueName(String base) {
        String candidate = base;
        int counter = 2;
        while (nameTaken(candidate)) {
            candidate = base + "_" + counter++;
        }
        return candidate;
    }

    private Result run(Function<Effects, Result> action) {
        Effects effects = new Effects();
        Result result;
        synchronized (lock) {
            result = action.apply(effects);
        }
        if (result.ok()) {
            Hooks current = hooks;
            if (effects.save) {
                current.saveRequested();
            }
            if (!effects.changed.isEmpty()) {
                current.changed(effects.changed);
            }
            for (Map.Entry<UUID, Result> note : effects.notes) {
                current.notifyPlayer(note.getKey(), note.getValue());
            }
        }
        return result;
    }

    public static String formatSeconds(long seconds) {
        long minutes = seconds / 60;
        long rest = seconds % 60;
        if (minutes >= 60) {
            return (minutes / 60) + "h " + (minutes % 60) + "m";
        }
        return minutes > 0 ? minutes + "m " + rest + "s" : rest + "s";
    }
}
