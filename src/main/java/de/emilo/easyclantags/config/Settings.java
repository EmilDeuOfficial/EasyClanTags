package de.emilo.easyclantags.config;

import de.emilo.easyclantags.model.TagFormat;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class Settings {

    public static final int EDITOR_MAX_LENGTH = 18;
    public static final int MENU_MAX_MEMBERS = 45;

    public enum StorageType { YAML, SQLITE }

    public enum SortMode { CLAN, NAME }

    public record GuiItem(Material material, int slot, String name) {
    }

    public final String language;
    public final boolean globalEnabled;
    public final StorageType storageType;
    public final String yamlFile;
    public final String sqliteFile;
    public final boolean chatEnabled;
    public final boolean tabEnabled;
    public final boolean nametagEnabled;
    public final String chatFormat;
    public final boolean allowPlayerFormatting;
    public final String nametagPrefix;
    public final String nametagSuffix;
    public final String tabFormat;
    public final boolean sortingEnabled;
    public final SortMode sortMode;
    public final boolean clanlessLast;
    public final int minLength;
    public final int maxLength;
    public final Pattern allowedCharacters;
    public final List<String> blacklist;
    public final String bracketOpen;
    public final String bracketClose;
    public final TextColor bracketColor;
    public final String separator;
    public final int defaultColor;
    public final int changeCooldownSeconds;
    public final boolean allowHexColors;
    public final boolean allowGradient;
    public final Map<TagFormat, Boolean> formatsEnabled;
    public final int minNameLength;
    public final int maxNameLength;
    public final Pattern namePattern;
    public final int maxMembers;
    public final int inviteTimeoutSeconds;
    public final int inputTimeoutSeconds;
    public final List<Integer> paletteSlots;
    private final Map<String, GuiItem> items;

    private Settings(FileConfiguration config, Logger log) {
        language = config.getString("language", "en").toLowerCase(Locale.ROOT);
        globalEnabled = config.getBoolean("global-enabled", true);

        StorageType type = StorageType.YAML;
        try {
            type = StorageType.valueOf(config.getString("storage.type", "YAML").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warning("Unknown storage.type, using YAML.");
        }
        storageType = type;
        yamlFile = config.getString("storage.yaml-file", "clans.yml");
        sqliteFile = config.getString("storage.sqlite-file", "clans.db");

        chatEnabled = config.getBoolean("areas.chat", true);
        tabEnabled = config.getBoolean("areas.tab", true);
        nametagEnabled = config.getBoolean("areas.nametag", true);

        chatFormat = config.getString("chat.format", "<tag><displayname><dark_gray> > <white><message>");
        allowPlayerFormatting = config.getBoolean("chat.allow-player-formatting", false);
        nametagPrefix = config.getString("nametag.prefix", "<tag>");
        nametagSuffix = config.getString("nametag.suffix", "");
        tabFormat = config.getString("tab.format", "<tag><player>");

        sortingEnabled = config.getBoolean("sorting.enabled", true);
        SortMode mode = SortMode.CLAN;
        try {
            mode = SortMode.valueOf(config.getString("sorting.mode", "CLAN").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warning("Unknown sorting.mode, using CLAN.");
        }
        sortMode = mode;
        clanlessLast = !"FIRST".equalsIgnoreCase(config.getString("sorting.clanless", "LAST"));

        int min = Math.max(1, config.getInt("tag.min-length", 2));
        int max = config.getInt("tag.max-length", 6);
        if (max > EDITOR_MAX_LENGTH) {
            log.warning("tag.max-length is capped at " + EDITOR_MAX_LENGTH + " because the editor shows one item per letter.");
            max = EDITOR_MAX_LENGTH;
        }
        max = Math.max(min, max);
        minLength = min;
        maxLength = max;
        allowedCharacters = compile(config.getString("tag.allowed-characters", "[A-Za-z0-9_]+"), "[A-Za-z0-9_]+", "tag.allowed-characters", log);
        List<String> words = new ArrayList<>();
        for (String word : config.getStringList("tag.blacklist")) {
            if (!word.isBlank()) {
                words.add(word.toLowerCase(Locale.ROOT));
            }
        }
        blacklist = List.copyOf(words);
        bracketOpen = config.getString("tag.brackets.open", "[");
        bracketClose = config.getString("tag.brackets.close", "]");
        bracketColor = parseColor(config.getString("tag.brackets.color", "#AAAAAA"), NamedTextColor.GRAY);
        separator = config.getString("tag.separator", " ");
        defaultColor = parseColor(config.getString("tag.default-color", "#FFFFFF"), NamedTextColor.WHITE).value();
        changeCooldownSeconds = Math.max(0, config.getInt("tag.change-cooldown-seconds", 300));
        allowHexColors = config.getBoolean("tag.allow-hex-colors", true);
        allowGradient = config.getBoolean("tag.allow-gradient", true);
        Map<TagFormat, Boolean> formats = new EnumMap<>(TagFormat.class);
        for (TagFormat format : TagFormat.values()) {
            formats.put(format, config.getBoolean("tag.formats." + format.key(), format != TagFormat.OBFUSCATED));
        }
        formatsEnabled = formats;

        minNameLength = Math.max(1, config.getInt("clan.min-name-length", 3));
        maxNameLength = Math.max(minNameLength, config.getInt("clan.max-name-length", 16));
        namePattern = compile(config.getString("clan.name-pattern", "[A-Za-z0-9_]+"), "[A-Za-z0-9_]+", "clan.name-pattern", log);
        int members = Math.max(2, config.getInt("clan.max-members", 20));
        if (members > MENU_MAX_MEMBERS) {
            log.warning("clan.max-members is capped at " + MENU_MAX_MEMBERS + " because the member menu has no pages.");
            members = MENU_MAX_MEMBERS;
        }
        maxMembers = members;
        inviteTimeoutSeconds = Math.max(5, config.getInt("clan.invite-timeout-seconds", 60));

        inputTimeoutSeconds = Math.max(5, config.getInt("gui.input-timeout-seconds", 60));
        List<Integer> palette = new ArrayList<>(config.getIntegerList("gui.palette-slots"));
        if (palette.size() < 16) {
            palette = List.of(18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 32, 33, 34);
        }
        paletteSlots = List.copyOf(palette);
        items = loadItems(config.getConfigurationSection("gui.items"), log);
    }

    public static Settings load(FileConfiguration config, Logger log) {
        return new Settings(config, log);
    }

    public GuiItem item(String key) {
        return items.getOrDefault(key, new GuiItem(Material.BARRIER, -1, ""));
    }

    public boolean formatEnabled(TagFormat format) {
        return formatsEnabled.getOrDefault(format, false);
    }

    private static Map<String, GuiItem> loadItems(ConfigurationSection section, Logger log) {
        Map<String, GuiItem> map = new HashMap<>();
        if (section == null) {
            return map;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            Material material = Material.matchMaterial(entry.getString("material", "STONE"));
            if (material == null || !(material.isItem() || material.isAir())) {
                log.warning("Unknown material for gui.items." + key + ", using STONE.");
                material = Material.STONE;
            }
            map.put(key, new GuiItem(material, entry.getInt("slot", -1), entry.getString("name", "")));
        }
        return map;
    }

    private static Pattern compile(String regex, String fallback, String path, Logger log) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            log.warning("Invalid regular expression in " + path + ", using " + fallback);
            return Pattern.compile(fallback);
        }
    }

    public static TextColor parseColor(String value, TextColor fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String trimmed = value.trim();
        TextColor parsed = trimmed.startsWith("#")
                ? TextColor.fromHexString(trimmed)
                : NamedTextColor.NAMES.value(trimmed.toLowerCase(Locale.ROOT));
        return parsed != null ? parsed : fallback;
    }
}
