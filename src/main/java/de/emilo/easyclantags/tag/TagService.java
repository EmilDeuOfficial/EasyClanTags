package de.emilo.easyclantags.tag;

import de.emilo.easyclantags.clan.ClanManager;
import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.Result;
import de.emilo.easyclantags.model.TagFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class TagService {

    public static final String PERM_HEX = "easyclantags.color.hex";
    public static final String PERM_GRADIENT = "easyclantags.gradient";
    public static final String PERM_BYPASS_BLACKLIST = "easyclantags.bypass.blacklist";
    public static final String PERM_BYPASS_COOLDOWN = "easyclantags.bypass.cooldown";

    public static final List<NamedTextColor> PALETTE = List.of(
            NamedTextColor.BLACK, NamedTextColor.DARK_BLUE, NamedTextColor.DARK_GREEN, NamedTextColor.DARK_AQUA,
            NamedTextColor.DARK_RED, NamedTextColor.DARK_PURPLE, NamedTextColor.GOLD, NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY, NamedTextColor.BLUE, NamedTextColor.GREEN, NamedTextColor.AQUA,
            NamedTextColor.RED, NamedTextColor.LIGHT_PURPLE, NamedTextColor.YELLOW, NamedTextColor.WHITE);

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Supplier<Settings> settings;
    private final ClanManager clans;

    public TagService(Supplier<Settings> settings, ClanManager clans) {
        this.settings = settings;
        this.clans = clans;
    }

    public Result validateText(String text, Predicate<String> has) {
        Settings s = settings.get();
        if (text == null || text.isEmpty()) {
            return Result.error("err.tag.empty");
        }
        int length = text.codePointCount(0, text.length());
        if (length < s.minLength || length > s.maxLength) {
            return Result.error("err.tag.length", "min", String.valueOf(s.minLength), "max", String.valueOf(s.maxLength));
        }
        if (!s.allowedCharacters.matcher(text).matches()) {
            return Result.error("err.tag.chars");
        }
        if (!has.test(PERM_BYPASS_BLACKLIST)) {
            String lower = text.toLowerCase(Locale.ROOT);
            for (String word : s.blacklist) {
                if (lower.contains(word)) {
                    return Result.error("err.tag.blacklisted", "word", word);
                }
            }
        }
        return Result.ok("ok.valid");
    }

    public Result validate(ClanTag tag, UUID ownClanId, Predicate<String> has) {
        Settings s = settings.get();
        Result text = validateText(tag.plain(), has);
        if (!text.ok()) {
            return text;
        }
        for (TagFormat format : TagFormat.values()) {
            if (!tag.formats().has(format)) {
                continue;
            }
            if (!s.formatEnabled(format)) {
                return Result.error("err.format.disabled", "format", format.key());
            }
            if (!has.test(format.permission())) {
                return Result.error("err.format.no-permission", "format", format.key());
            }
        }
        if (!hexAllowed(has)) {
            for (int rgb : tag.colors()) {
                if (!isPaletteColor(rgb)) {
                    return Result.error("err.color.hex-denied");
                }
            }
        }
        ClanData holder = clans.byTag(tag.plain()).orElse(null);
        if (holder != null && !holder.id().equals(ownClanId)) {
            return Result.error("err.tag.taken", "tag", tag.plain(), "clan", holder.name());
        }
        return Result.ok("ok.valid");
    }

    public boolean hexAllowed(Predicate<String> has) {
        return settings.get().allowHexColors && has.test(PERM_HEX);
    }

    public Result checkGradient(Predicate<String> has) {
        Settings s = settings.get();
        if (!s.allowGradient) {
            return Result.error("err.gradient.disabled");
        }
        if (!has.test(PERM_GRADIENT)) {
            return Result.error("err.gradient.no-permission");
        }
        if (!hexAllowed(has)) {
            return Result.error("err.gradient.needs-hex");
        }
        return Result.ok("ok.valid");
    }

    public Result checkFormat(TagFormat format, Predicate<String> has) {
        if (!settings.get().formatEnabled(format)) {
            return Result.error("err.format.disabled", "format", format.key());
        }
        if (!has.test(format.permission())) {
            return Result.error("err.format.no-permission", "format", format.key());
        }
        return Result.ok("ok.valid");
    }

    public ClanTag gradient(ClanTag tag, int from, int to) {
        int count = tag.length();
        if (count == 0) {
            return tag;
        }
        String markup = "<gradient:" + toHex(from) + ":" + toHex(to) + ">"
                + MINI_MESSAGE.escapeTags(tag.plain()) + "</gradient>";
        List<Integer> colors = new ArrayList<>(count);
        collectColors(MINI_MESSAGE.deserialize(markup), null, colors);
        if (colors.size() != count) {
            colors.clear();
            for (int i = 0; i < count; i++) {
                float ratio = count <= 1 ? 0f : (float) i / (count - 1);
                colors.add(TextColor.lerp(ratio, TextColor.color(from), TextColor.color(to)).value());
            }
        }
        return tag.withColors(colors);
    }

    private static void collectColors(Component component, TextColor inherited, List<Integer> out) {
        TextColor color = component.color() != null ? component.color() : inherited;
        if (component instanceof TextComponent text) {
            text.content().codePoints().forEach(codePoint ->
                    out.add(color == null ? 0xFFFFFF : color.value()));
        }
        for (Component child : component.children()) {
            collectColors(child, color, out);
        }
    }

    public static boolean isPaletteColor(int rgb) {
        for (NamedTextColor color : PALETTE) {
            if (color.value() == rgb) {
                return true;
            }
        }
        return false;
    }

    public static Optional<Integer> parseHex(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.length() == 3) {
            trimmed = "" + trimmed.charAt(0) + trimmed.charAt(0) + trimmed.charAt(1) + trimmed.charAt(1)
                    + trimmed.charAt(2) + trimmed.charAt(2);
        }
        if (trimmed.length() != 6) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(trimmed, 16));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static String toHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }
}
