package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

final class ConfirmMenu extends Menu {

    private final Component title;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    ConfirmMenu(EasyClanTags plugin, GuiManager gui, Player player, Component title, Runnable onConfirm, Runnable onCancel) {
        super(plugin, gui, player);
        this.title = title;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    protected Component title() {
        return title;
    }

    @Override
    protected int rows() {
        return 3;
    }

    @Override
    protected void build() {
        ItemFactory items = gui.items();
        set(items.slot("dialog-confirm"), items.create("dialog-confirm"), event -> onConfirm.run());
        set(items.slot("dialog-cancel"), items.create("dialog-cancel"), event -> onCancel.run());
    }
}
