package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Menu implements InventoryHolder {

    protected final EasyClanTags plugin;
    protected final GuiManager gui;
    protected final Player player;
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();
    private Inventory inventory;

    protected Menu(EasyClanTags plugin, GuiManager gui, Player player) {
        this.plugin = plugin;
        this.gui = gui;
        this.player = player;
    }

    protected abstract Component title();

    protected abstract int rows();

    protected abstract void build();

    public void open() {
        inventory = Bukkit.createInventory(this, rows() * 9, title());
        redraw();
        player.openInventory(inventory);
    }

    public void redraw() {
        inventory.clear();
        actions.clear();
        build();
        ItemStack filler = gui.items().filler();
        if (filler != null) {
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (inventory.getItem(slot) == null) {
                    inventory.setItem(slot, filler.clone());
                }
            }
        }
    }

    protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        inventory.setItem(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    protected void set(int slot, ItemStack item) {
        set(slot, item, null);
    }

    void handleClick(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> action = actions.get(event.getSlot());
        if (action != null) {
            action.accept(event);
        }
    }

    public void onClose() {
    }

    protected boolean allowed(String permission) {
        if (player.hasPermission(permission)) {
            return true;
        }
        plugin.lang().send(player, "err.no-permission");
        return false;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
