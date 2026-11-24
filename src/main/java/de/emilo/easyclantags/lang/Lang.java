package de.emilo.easyclantags.lang;

import de.emilo.easyclantags.model.Result;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Lang {

    public static final List<String> CODES = List.of("en", "de", "ru", "es", "fr", "ja", "zh");

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> texts = new HashMap<>();
    private final Map<String, List<String>> lists = new HashMap<>();
    private final String code;

    public Lang(JavaPlugin plugin, String requestedCode) {
        this.code = CODES.contains(requestedCode) ? requestedCode : "en";
        if (!CODES.contains(requestedCode)) {
            plugin.getLogger().warning("Unknown language '" + requestedCode + "', using en.");
        }
        extractBundledFiles(plugin);
        loadBundled(plugin, "en");
        if (!code.equals("en")) {
            loadBundled(plugin, code);
        }
        File custom = new File(plugin.getDataFolder(), "lang/messages_" + code + ".yml");
        if (custom.isFile()) {
            merge(YamlConfiguration.loadConfiguration(custom));
        }
    }

    public String code() {
        return code;
    }

    public String raw(String key) {
        return texts.getOrDefault(key, key);
    }

    public boolean has(String key) {
        return texts.containsKey(key) || lists.containsKey(key);
    }

    public Component component(String key, TagResolver... resolvers) {
        return miniMessage.deserialize(raw(key), TagResolver.resolver(resolvers));
    }

    public Component prefixed(String key, TagResolver... resolvers) {
        return miniMessage.deserialize(raw("prefix") + raw(key), TagResolver.resolver(resolvers));
    }

    public List<Component> lines(String key, TagResolver... resolvers) {
        List<Component> result = new ArrayList<>();
        TagResolver resolver = TagResolver.resolver(resolvers);
        for (String line : lists.getOrDefault(key, List.of())) {
            result.add(miniMessage.deserialize(line, resolver));
        }
        return result;
    }

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        sender.sendMessage(prefixed(key, resolvers));
    }

    public void send(CommandSender sender, Result result) {
        sender.sendMessage(prefixed(result.key(), result.resolvers()));
    }

    private void extractBundledFiles(JavaPlugin plugin) {
        for (String bundled : CODES) {
            File target = new File(plugin.getDataFolder(), "lang/messages_" + bundled + ".yml");
            if (!target.exists()) {
                plugin.saveResource("lang/messages_" + bundled + ".yml", false);
            }
        }
    }

    private void loadBundled(JavaPlugin plugin, String bundledCode) {
        try (InputStream stream = plugin.getResource("lang/messages_" + bundledCode + ".yml")) {
            if (stream != null) {
                merge(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not read language file " + bundledCode + ": " + e.getMessage());
        }
    }

    private void merge(YamlConfiguration yaml) {
        for (String key : yaml.getKeys(true)) {
            if (yaml.isList(key)) {
                lists.put(key, yaml.getStringList(key));
            } else if (yaml.isString(key)) {
                texts.put(key, yaml.getString(key, key));
            }
        }
    }
}
