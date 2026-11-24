package de.emilo.easyclantags.storage;

import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.TagFormats;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class YamlStorage implements Storage {

    private final Path file;

    public YamlStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<ClanData> load() throws IOException {
        List<ClanData> result = new ArrayList<>();
        if (!Files.isRegularFile(file)) {
            return result;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
        } catch (InvalidConfigurationException e) {
            throw new IOException("Invalid YAML in " + file.getFileName() + ": " + e.getMessage(), e);
        }
        ConfigurationSection clans = yaml.getConfigurationSection("clans");
        if (clans == null) {
            return result;
        }
        for (String key : clans.getKeys(false)) {
            ConfigurationSection section = clans.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            try {
                result.add(readClan(UUID.fromString(key), section));
            } catch (IllegalArgumentException e) {
                throw new IOException("Broken clan entry " + key + " in " + file.getFileName(), e);
            }
        }
        return result;
    }

    private ClanData readClan(UUID id, ConfigurationSection section) {
        Map<UUID, String> members = new LinkedHashMap<>();
        ConfigurationSection memberSection = section.getConfigurationSection("members");
        if (memberSection != null) {
            for (String memberKey : memberSection.getKeys(false)) {
                members.put(UUID.fromString(memberKey), memberSection.getString(memberKey, "?"));
            }
        }
        TagFormats formats = TagFormats.fromKeys(section.getStringList("tag.formats"));
        ClanTag tag = TagCodec.build(section.getString("tag.text", ""),
                TagCodec.colorsFromList(section.getStringList("tag.colors")), formats);
        return new ClanData(id, section.getString("name", "clan"), UUID.fromString(section.getString("owner", "")),
                members, tag, section.getLong("tag-changed", 0L), section.getLong("created", 0L));
    }

    @Override
    public void save(Collection<ClanData> clans) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        for (ClanData clan : clans) {
            String base = "clans." + clan.id();
            yaml.set(base + ".name", clan.name());
            yaml.set(base + ".owner", clan.owner().toString());
            yaml.set(base + ".created", clan.createdAt());
            yaml.set(base + ".tag-changed", clan.tagChangedAt());
            yaml.set(base + ".tag.text", clan.tag().plain());
            yaml.set(base + ".tag.colors", TagCodec.colorsToList(clan.tag()));
            yaml.set(base + ".tag.formats", clan.tag().formats().keys());
            for (Map.Entry<UUID, String> member : clan.members().entrySet()) {
                yaml.set(base + ".members." + member.getKey(), member.getValue());
            }
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, yaml.saveToString(), StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public String describe() {
        return "YAML (" + file.getFileName() + ")";
    }
}
