package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.config.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemFactory {

    private final EasyClanTags plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ItemFactory(EasyClanTags plugin) {
        this.plugin = plugin;
    }

    public ItemStack create(String key, TagResolver... resolvers) {
        return create(key, "", plugin.settings().item(key).material(), resolvers);
    }

    public ItemStack create(String key, String variant, TagResolver... resolvers) {
        return create(key, variant, plugin.settings().item(key).material(), resolvers);
    }

    public ItemStack create(String key, String variant, Material material, TagResolver... resolvers) {
        Settings.GuiItem configured = plugin.settings().item(key);
        TagResolver resolver = TagResolver.resolver(resolvers);
        Component name = configured.name().isBlank()
                ? plugin.lang().component("gui.item." + key + ".name", resolver)
                : miniMessage.deserialize(configured.name(), resolver);
        List<Component> lore = new ArrayList<>();
        for (Component line : plugin.lang().lines("gui.item." + key + ".lore" + variant, resolver)) {
            lore.add(plain(line));
        }
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(plain(name));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack glint(ItemStack stack, boolean enabled) {
        ItemMeta meta = stack.getItemMeta();
        meta.setEnchantmentGlintOverride(enabled ? Boolean.TRUE : null);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack filler() {
        Material material = plugin.settings().item("filler").material();
        if (material.isAir()) {
            return null;
        }
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false));
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return stack;
    }

    public int slot(String key) {
        return plugin.settings().item(key).slot();
    }

    private Component plain(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
