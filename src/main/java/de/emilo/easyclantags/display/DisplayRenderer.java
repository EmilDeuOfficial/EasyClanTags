package de.emilo.easyclantags.display;

import de.emilo.easyclantags.config.Settings;
import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.TagFormat;
import de.emilo.easyclantags.model.TagLetter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class DisplayRenderer {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Supplier<Settings> settings;

    public DisplayRenderer(Supplier<Settings> settings) {
        this.settings = settings;
    }

    public Component bare(ClanTag tag) {
        Settings s = settings.get();
        Component result = Component.empty();
        for (TagLetter letter : tag.letters()) {
            Component part = Component.text(letter.text(), TextColor.color(letter.rgb()));
            for (TagFormat format : TagFormat.values()) {
                if (tag.formats().has(format) && s.formatEnabled(format)) {
                    part = part.decoration(format.decoration(), true);
                }
            }
            result = result.append(part);
        }
        return result;
    }

    public Component bracketed(ClanTag tag) {
        Settings s = settings.get();
        return Component.empty()
                .append(Component.text(s.bracketOpen, s.bracketColor))
                .append(bare(tag))
                .append(Component.text(s.bracketClose, s.bracketColor));
    }

    public TagResolver resolvers(String clanName, ClanTag tag, String playerName, Component displayName, Component message) {
        Settings s = settings.get();
        boolean hasTag = tag != null && !tag.isEmpty();
        Component bracketed = hasTag ? bracketed(tag) : Component.empty();
        Component trailing = hasTag ? bracketed.append(Component.text(s.separator)) : Component.empty();
        Component leading = hasTag ? Component.text(s.separator).append(bracketed) : Component.empty();
        List<TagResolver> list = new ArrayList<>();
        list.add(Placeholder.component("tag", trailing));
        list.add(Placeholder.component("tag_leading", leading));
        list.add(Placeholder.component("tag_bare", hasTag ? bare(tag) : Component.empty()));
        list.add(Placeholder.unparsed("clan", clanName == null ? "" : clanName));
        list.add(Placeholder.unparsed("player", playerName));
        list.add(Placeholder.component("displayname", displayName == null ? Component.text(playerName) : displayName));
        if (message != null) {
            list.add(Placeholder.component("message", message));
        }
        return TagResolver.resolver(list);
    }

    public Component chat(String clanName, ClanTag tag, String playerName, Component displayName, Component message) {
        return miniMessage.deserialize(settings.get().chatFormat, resolvers(clanName, tag, playerName, displayName, message));
    }

    public Component nametagPrefix(String clanName, ClanTag tag, String playerName) {
        return render(settings.get().nametagPrefix, clanName, tag, playerName);
    }

    public Component nametagSuffix(String clanName, ClanTag tag, String playerName) {
        return render(settings.get().nametagSuffix, clanName, tag, playerName);
    }

    public Component tab(String clanName, ClanTag tag, String playerName) {
        return render(settings.get().tabFormat, clanName, tag, playerName);
    }

    public Component nametagFull(String clanName, ClanTag tag, String playerName) {
        return nametagPrefix(clanName, tag, playerName)
                .append(Component.text(playerName))
                .append(nametagSuffix(clanName, tag, playerName));
    }

    private Component render(String format, String clanName, ClanTag tag, String playerName) {
        if (format == null || format.isEmpty()) {
            return Component.empty();
        }
        return miniMessage.deserialize(format, resolvers(clanName, tag, playerName, null, null));
    }
}
