package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.EasyClanTags;
import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.Result;
import de.emilo.easyclantags.model.TagFormat;
import de.emilo.easyclantags.model.TagLetter;
import de.emilo.easyclantags.tag.TagService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class TagEditorMenu extends Menu {

    private static final List<Material> PALETTE_MATERIALS = List.of(
            Material.BLACK_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL, Material.CYAN_WOOL,
            Material.RED_WOOL, Material.PURPLE_WOOL, Material.ORANGE_WOOL, Material.LIGHT_GRAY_WOOL,
            Material.GRAY_WOOL, Material.LIGHT_BLUE_WOOL, Material.LIME_WOOL, Material.DIAMOND_BLOCK,
            Material.RED_CONCRETE, Material.MAGENTA_WOOL, Material.YELLOW_WOOL, Material.WHITE_WOOL);

    private final EditorSession session;

    TagEditorMenu(EasyClanTags plugin, GuiManager gui, Player player, EditorSession session) {
        super(plugin, gui, player);
        this.session = session;
    }

    @Override
    protected Component title() {
        return plugin.lang().component("gui.editor.title");
    }

    @Override
    protected int rows() {
        return 6;
    }

    @Override
    public void onClose() {
        gui.editorClosed(player);
    }

    @Override
    protected void build() {
        ItemFactory items = gui.items();
        Settings settings = plugin.settings();
        ClanTag tag = session.tag;

        for (int i = 0; i < tag.length() && i < Settings.EDITOR_MAX_LENGTH; i++) {
            TagLetter letter = tag.letters().get(i);
            int index = i;
            boolean selected = i == session.selected;
            Component shown = plugin.renderer().bare(new ClanTag(List.of(letter), tag.formats()));
            ItemStack item = items.create("letter", selected ? "-selected" : "",
                    Placeholder.component("letter", shown),
                    Placeholder.unparsed("index", String.valueOf(i + 1)),
                    Placeholder.unparsed("hex", TagService.toHex(letter.rgb())));
            items.glint(item, selected);
            set(i, item, event -> {
                session.selected = index;
                redraw();
            });
        }

        List<Integer> slots = settings.paletteSlots;
        for (int i = 0; i < TagService.PALETTE.size(); i++) {
            NamedTextColor color = TagService.PALETTE.get(i);
            String hex = TagService.toHex(color.value());
            ItemStack item = items.create("palette", "", PALETTE_MATERIALS.get(i),
                    Placeholder.component("color", Component.text(hex, color)),
                    Placeholder.unparsed("hex", hex));
            set(slots.get(i), item, event -> pickColor(color.value()));
        }

        set(items.slot("hex-color"), items.create("hex-color"), event -> promptHex());
        set(items.slot("apply-all"), items.create("apply-all"), event -> applyToAll());

        String gradientVariant = switch (session.mode) {
            case NORMAL -> "";
            case GRADIENT_START -> "-start";
            case GRADIENT_END -> "-end";
        };
        ItemStack gradient = items.create("gradient", gradientVariant);
        items.glint(gradient, session.mode != EditorSession.Mode.NORMAL);
        set(items.slot("gradient"), gradient, event -> toggleGradient());

        for (TagFormat format : TagFormat.values()) {
            boolean active = tag.formats().has(format);
            Component state = plugin.lang().component(active ? "gui.state.on" : "gui.state.off");
            ItemStack item = items.create("format-" + format.key(), "", Placeholder.component("state", state));
            items.glint(item, active);
            set(items.slot("format-" + format.key()), item, event -> toggleFormat(format));
        }

        set(items.slot("change-text"), items.create("change-text"), event -> changeText());
        set(items.slot("cancel"), items.create("cancel"), event -> {
            gui.endSession(player);
            gui.openMain(player);
        });
        set(items.slot("editor-preview"), items.create("editor-preview", "",
                gui.previewResolvers(player, clanName(), tag)));
        set(items.slot("confirm"), items.create("confirm"), event -> confirm());
    }

    private String clanName() {
        return plugin.clans().clanOf(player.getUniqueId()).map(ClanData::name).orElse("");
    }

    private void pickColor(int rgb) {
        applyColor(rgb);
        redraw();
    }

    private void applyColor(int rgb) {
        switch (session.mode) {
            case NORMAL -> session.tag = session.tag.withColor(session.selected, rgb);
            case GRADIENT_START -> {
                session.gradientStart = rgb;
                session.mode = EditorSession.Mode.GRADIENT_END;
                player.sendActionBar(plugin.lang().component("gui.gradient.pick-end"));
            }
            case GRADIENT_END -> {
                session.tag = plugin.tags().gradient(session.tag, session.gradientStart, rgb);
                session.mode = EditorSession.Mode.NORMAL;
            }
        }
    }

    private void applyToAll() {
        if (session.tag.isEmpty() || session.selected >= session.tag.length()) {
            return;
        }
        int rgb = session.tag.letters().get(session.selected).rgb();
        session.tag = session.tag.withAllColors(rgb);
        redraw();
    }

    private void toggleGradient() {
        if (session.mode != EditorSession.Mode.NORMAL) {
            session.mode = EditorSession.Mode.NORMAL;
            redraw();
            return;
        }
        Result result = plugin.tags().checkGradient(player::hasPermission);
        if (!result.ok()) {
            plugin.lang().send(player, result);
            return;
        }
        session.mode = EditorSession.Mode.GRADIENT_START;
        player.sendActionBar(plugin.lang().component("gui.gradient.pick-start"));
        redraw();
    }

    private void toggleFormat(TagFormat format) {
        boolean active = session.tag.formats().has(format);
        if (!active) {
            Result result = plugin.tags().checkFormat(format, player::hasPermission);
            if (!result.ok()) {
                plugin.lang().send(player, result);
                return;
            }
        }
        session.tag = session.tag.withFormats(session.tag.formats().with(format, !active));
        redraw();
    }

    private void promptHex() {
        if (!plugin.tags().hexAllowed(player::hasPermission)) {
            plugin.lang().send(player, "err.color.hex-denied");
            return;
        }
        gui.prompt(player, "input.hex", text -> {
            Optional<Integer> parsed = TagService.parseHex(text);
            if (parsed.isEmpty()) {
                plugin.lang().send(player, "err.color.invalid-hex");
            } else {
                applyColor(parsed.get());
            }
            gui.reopenEditor(player);
        }, () -> gui.reopenEditor(player));
    }

    private void changeText() {
        gui.prompt(player, "input.tag-text", text -> {
            Result result = plugin.tags().validateText(text, player::hasPermission);
            if (!result.ok()) {
                plugin.lang().send(player, result);
            } else {
                session.tag = session.tag.withText(text, plugin.settings().defaultColor);
                session.clampSelection();
            }
            gui.reopenEditor(player);
        }, () -> gui.reopenEditor(player));
    }

    private void confirm() {
        UUID id = player.getUniqueId();
        ClanData clan = plugin.clans().clanOf(id).orElse(null);
        if (clan == null) {
            plugin.lang().send(player, "err.clan.none");
            return;
        }
        Result valid = plugin.tags().validate(session.tag, clan.id(), player::hasPermission);
        if (!valid.ok()) {
            plugin.lang().send(player, valid);
            return;
        }
        Result result = plugin.clans().setTag(id, session.tag, player.hasPermission(TagService.PERM_BYPASS_COOLDOWN));
        plugin.lang().send(player, result);
        if (result.ok()) {
            gui.endSession(player);
            gui.openMain(player);
        }
    }
}
