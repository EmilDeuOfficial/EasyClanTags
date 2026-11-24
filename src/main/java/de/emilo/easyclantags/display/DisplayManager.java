package de.emilo.easyclantags.display;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class DisplayManager {

    private static final String MARKER = "ect";
    private static final Pattern OWN_TEAM = Pattern.compile("^ect.{4}[0-9a-f]{8}$");

    private final EasyClanTags plugin;
    private final DisplayRenderer renderer;
    private final Map<UUID, String> teamNames = new HashMap<>();
    private final Map<UUID, Component> tabNames = new HashMap<>();
    private final Set<String> warned = new HashSet<>();
    private Set<Scoreboard> knownBoards = new HashSet<>();

    public DisplayManager(EasyClanTags plugin, DisplayRenderer renderer) {
        this.plugin = plugin;
        this.renderer = renderer;
    }

    public void refreshAll() {
        plugin.runSync(() -> {
            Collection<Scoreboard> boards = boards();
            knownBoards = new HashSet<>(boards);
            Map<UUID, Integer> ranks = ranks();
            for (Player player : Bukkit.getOnlinePlayers()) {
                apply(player, boards, ranks);
            }
        });
    }

    public void refresh(Collection<UUID> players) {
        refreshAll();
    }

    public void checkBoards() {
        if (!plugin.isActive()) {
            return;
        }
        if (!new HashSet<>(boards()).equals(knownBoards)) {
            refreshAll();
        }
    }

    public void playerQuit(Player player) {
        plugin.runSync(() -> {
            String team = teamNames.remove(player.getUniqueId());
            tabNames.remove(player.getUniqueId());
            if (team != null) {
                for (Scoreboard board : boards()) {
                    unregister(board, team);
                }
            }
        });
    }

    public void shutdown() {
        Runnable task = () -> {
            for (Scoreboard board : boards()) {
                for (Team team : new ArrayList<>(board.getTeams())) {
                    if (OWN_TEAM.matcher(team.getName()).matches()) {
                        team.unregister();
                    }
                }
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playerListName(null);
            }
            teamNames.clear();
            tabNames.clear();
            warned.clear();
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            plugin.runSync(task);
        }
    }

    private void apply(Player player, Collection<Scoreboard> boards, Map<UUID, Integer> ranks) {
        Settings s = plugin.settings();
        if (!plugin.isActive()) {
            return;
        }
        UUID id = player.getUniqueId();
        ClanData clan = plugin.clans().clanOf(id).orElse(null);
        ClanTag tag = clan == null ? ClanTag.EMPTY : clan.tag();
        String clanName = clan == null ? null : clan.name();
        String playerName = player.getName();

        if (s.tabEnabled) {
            Component tab = renderer.tab(clanName, tag, playerName);
            if (!tab.equals(tabNames.get(id))) {
                player.playerListName(tab);
                tabNames.put(id, tab);
            }
        } else if (tabNames.remove(id) != null) {
            player.playerListName(null);
        }

        boolean tagged = clan != null && !tag.isEmpty();
        boolean wantTeam = s.sortingEnabled || (s.nametagEnabled && tagged);
        String desired = wantTeam ? teamName(player, clan, ranks, s) : null;
        String old = teamNames.get(id);
        if (old != null && !old.equals(desired)) {
            for (Scoreboard board : boards) {
                unregister(board, old);
            }
            teamNames.remove(id);
        }
        if (desired == null) {
            return;
        }
        Component prefix = s.nametagEnabled ? renderer.nametagPrefix(clanName, tag, playerName) : Component.empty();
        Component suffix = s.nametagEnabled ? renderer.nametagSuffix(clanName, tag, playerName) : Component.empty();
        boolean placed = false;
        for (Scoreboard board : boards) {
            placed |= syncTeam(board, player, desired, prefix, suffix);
        }
        if (placed) {
            teamNames.put(id, desired);
        }
    }

    private boolean syncTeam(Scoreboard board, Player player, String teamName, Component prefix, Component suffix) {
        String entry = player.getName();
        Team current = board.getEntryTeam(entry);
        if (current != null && !current.getName().equals(teamName) && !OWN_TEAM.matcher(current.getName()).matches()) {
            if (warned.add(player.getUniqueId() + ":" + current.getName())) {
                plugin.getLogger().warning(entry + " is in the scoreboard team '" + current.getName()
                        + "' from another plugin. Nametag and tab sorting are skipped for this player.");
            }
            return false;
        }
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }
        if (!team.hasEntry(entry)) {
            team.addEntry(entry);
        }
        if (!prefix.equals(team.prefix())) {
            team.prefix(prefix);
        }
        if (!suffix.equals(team.suffix())) {
            team.suffix(suffix);
        }
        return true;
    }

    private void unregister(Scoreboard board, String name) {
        Team team = board.getTeam(name);
        if (team != null) {
            team.unregister();
        }
    }

    private Collection<Scoreboard> boards() {
        Set<Scoreboard> boards = new LinkedHashSet<>();
        if (Bukkit.getScoreboardManager() != null) {
            boards.add(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            boards.add(player.getScoreboard());
        }
        return boards;
    }

    private Map<UUID, Integer> ranks() {
        List<ClanData> tagged = new ArrayList<>();
        for (ClanData clan : plugin.clans().snapshot()) {
            if (!clan.tag().isEmpty()) {
                tagged.add(clan);
            }
        }
        tagged.sort(Comparator.comparing((ClanData clan) -> clan.tag().normalized()).thenComparing(ClanData::name));
        Map<UUID, Integer> ranks = new HashMap<>();
        for (int i = 0; i < tagged.size(); i++) {
            ranks.put(tagged.get(i).id(), i);
        }
        return ranks;
    }

    private String teamName(Player player, ClanData clan, Map<UUID, Integer> ranks, Settings s) {
        String namePart = alphanumeric(player.getName(), s.sortMode == Settings.SortMode.NAME ? 4 : 2);
        String key;
        if (s.sortMode == Settings.SortMode.NAME) {
            key = namePart;
        } else if (clan != null && ranks.containsKey(clan.id())) {
            key = base36(ranks.get(clan.id())) + namePart;
        } else {
            key = (s.clanlessLast ? "zz" : "00") + namePart;
        }
        String unique = player.getUniqueId().toString().replace("-", "").substring(0, 8);
        return MARKER + key + unique;
    }

    private static String alphanumeric(String value, int length) {
        StringBuilder builder = new StringBuilder();
        for (char c : value.toLowerCase(Locale.ROOT).toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                builder.append(c);
            }
            if (builder.length() == length) {
                break;
            }
        }
        while (builder.length() < length) {
            builder.append('0');
        }
        return builder.toString();
    }

    private static String base36(int value) {
        String text = Integer.toString(Math.min(value, 1295), 36);
        return text.length() < 2 ? "0" + text : text;
    }
}
