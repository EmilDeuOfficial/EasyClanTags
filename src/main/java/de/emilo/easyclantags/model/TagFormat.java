package de.emilo.easyclantags.model;

import net.kyori.adventure.text.format.TextDecoration;

import java.util.Locale;
import java.util.Optional;

public enum TagFormat {
    BOLD(TextDecoration.BOLD),
    ITALIC(TextDecoration.ITALIC),
    UNDERLINE(TextDecoration.UNDERLINED),
    STRIKETHROUGH(TextDecoration.STRIKETHROUGH),
    OBFUSCATED(TextDecoration.OBFUSCATED);

    private final TextDecoration decoration;

    TagFormat(TextDecoration decoration) {
        this.decoration = decoration;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public TextDecoration decoration() {
        return decoration;
    }

    public String permission() {
        return "easyclantags.format." + key();
    }

    public static Optional<TagFormat> byKey(String key) {
        for (TagFormat format : values()) {
            if (format.key().equalsIgnoreCase(key)) {
                return Optional.of(format);
            }
        }
        return Optional.empty();
    }
}
