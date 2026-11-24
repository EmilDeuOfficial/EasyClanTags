package de.emilo.easyclantags;

import de.emilo.easyclantags.clan.ClanManager;
import de.emilo.easyclantags.command.ClanTagCommand;
import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.display.DisplayManager;
import de.emilo.easyclantags.display.DisplayRenderer;
import de.emilo.easyclantags.gui.GuiManager;
import de.emilo.easyclantags.gui.InputService;
import de.emilo.easyclantags.lang.Lang;
import de.emilo.easyclantags.listener.ChatListener;
import de.emilo.easyclantags.listener.PlayerListener;
import de.emilo.easyclantags.model.Result;
import de.emilo.easyclantags.storage.SqliteStorage;
import de.emilo.easyclantags.storage.Storage;
import de.emilo.easyclantags.storage.StorageService;
import de.emilo.easyclantags.storage.YamlStorage;
import de.emilo.easyclantags.tag.TagService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;

public final class EasyClanTags extends JavaPlugin {

    private volatile Settings settings;
    private volatile boolean active;
    private Lang lang;
    private ClanManager clans;
    private TagService tags;
    private DisplayRenderer renderer;
    private DisplayManager display;
    private InputService input;
    private GuiManager gui;
    private StorageService storage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = Settings.load(getConfig(), getLogger());
        lang = new Lang(this, settings.language);
        clans = new ClanManager(this::settings);
        tags = new TagService(this::settings, clans);
        renderer = new DisplayRenderer(this::settings);
        display = new DisplayManager(this, renderer);
        input = new InputService(this);
        gui = new GuiManager(this);

        storage = createStorage();
        try {
            clans.load(storage.load());
        } catch (IOException e) {
            getLogger().severe("Could not load clans from " + storage.describe() + ": " + e.getMessage());
            getLogger().severe("Disabling the plugin so the stored data is not overwritten. Fix or move the file and restart.");
            storage.shutdownWithoutSaving();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        clans.setHooks(new ClanManager.Hooks() {
            @Override
            public void changed(Collection<UUID> players) {
                display.refreshAll();
            }

            @Override
            public void notifyPlayer(UUID player, Result message) {
                Player online = getServer().getPlayer(player);
                if (online != null) {
                    lang.send(online, message);
                }
            }

            @Override
            public void saveRequested() {
                storage.requestSave();
            }
        });

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(gui, this);

        ClanTagCommand executor = new ClanTagCommand(this);
        PluginCommand command = getCommand("clantag");
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        active = settings.globalEnabled;
        display.shutdown();
        getServer().getGlobalRegionScheduler().runAtFixedRate(this, task -> display.checkBoards(), 100L, 100L);
        display.refreshAll();
        getLogger().info("EasyClanTags enabled. Clans: " + clans.clanCount() + ", storage: " + storage.describe());
    }

    @Override
    public void onDisable() {
        if (display == null) {
            return;
        }
        getServer().getGlobalRegionScheduler().cancelTasks(this);
        gui.closeAll();
        input.clear();
        display.shutdown();
        storage.shutdown();
    }

    public void reloadAll() {
        reloadConfig();
        Settings next = Settings.load(getConfig(), getLogger());
        boolean storageChanged = next.storageType != settings.storageType
                || !next.yamlFile.equals(settings.yamlFile) || !next.sqliteFile.equals(settings.sqliteFile);
        settings = next;
        lang = new Lang(this, next.language);
        if (storageChanged) {
            storage.shutdown();
            storage = createStorage();
            storage.requestSave();
        }
        gui.closeAll();
        input.clear();
        display.shutdown();
        active = next.globalEnabled;
        display.refreshAll();
    }

    public void setGlobalEnabled(boolean enabled) {
        active = enabled;
        getConfig().set("global-enabled", enabled);
        saveConfig();
        gui.closeAll();
        display.shutdown();
        if (enabled) {
            display.refreshAll();
        }
    }

    public void runSync(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else if (isEnabled()) {
            getServer().getGlobalRegionScheduler().run(this, scheduled -> task.run());
        }
    }

    private StorageService createStorage() {
        Path folder = getDataFolder().toPath();
        Storage backend = null;
        if (settings.storageType == Settings.StorageType.SQLITE) {
            try {
                backend = new SqliteStorage(folder.resolve(settings.sqliteFile), getClassLoader());
            } catch (IOException e) {
                getLogger().severe(e.getMessage() + ". Falling back to YAML storage.");
            }
        }
        if (backend == null) {
            backend = new YamlStorage(folder.resolve(settings.yamlFile));
        }
        return new StorageService(backend, clans::snapshot, getLogger());
    }

    public boolean isActive() {
        return active;
    }

    public Settings settings() {
        return settings;
    }

    public Lang lang() {
        return lang;
    }

    public ClanManager clans() {
        return clans;
    }

    public TagService tags() {
        return tags;
    }

    public DisplayRenderer renderer() {
        return renderer;
    }

    public DisplayManager display() {
        return display;
    }

    public InputService input() {
        return input;
    }

    public GuiManager gui() {
        return gui;
    }

    public StorageService storage() {
        return storage;
    }
}
