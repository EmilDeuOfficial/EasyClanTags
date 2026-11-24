package de.emilo.easyclantags.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record ClanData(UUID id, String name, UUID owner, Map<UUID, String> members, ClanTag tag,
                       long tagChangedAt, long createdAt) {

    public ClanData {
        members = Collections.unmodifiableMap(new LinkedHashMap<>(members));
    }

    public boolean isMember(UUID player) {
        return members.containsKey(player);
    }

    public ClanData withName(String newName) {
        return new ClanData(id, newName, owner, members, tag, tagChangedAt, createdAt);
    }

    public ClanData withOwner(UUID newOwner) {
        return new ClanData(id, name, newOwner, members, tag, tagChangedAt, createdAt);
    }

    public ClanData withMembers(Map<UUID, String> newMembers) {
        return new ClanData(id, name, owner, newMembers, tag, tagChangedAt, createdAt);
    }

    public ClanData withTag(ClanTag newTag, long changedAt) {
        return new ClanData(id, name, owner, members, newTag, changedAt, createdAt);
    }

    public ClanData withMember(UUID player, String playerName) {
        Map<UUID, String> next = new LinkedHashMap<>(members);
        next.put(player, playerName);
        return withMembers(next);
    }

    public ClanData withoutMember(UUID player) {
        Map<UUID, String> next = new LinkedHashMap<>(members);
        next.remove(player);
        return withMembers(next);
    }
}
