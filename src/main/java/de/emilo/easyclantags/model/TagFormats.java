package de.emilo.easyclantags.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record TagFormats(Set<TagFormat> active) {

    public static final TagFormats NONE = new TagFormats(EnumSet.noneOf(TagFormat.class));

    public TagFormats {
        active = active.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(active));
    }

    public boolean has(TagFormat format) {
        return active.contains(format);
    }

    public boolean isEmpty() {
        return active.isEmpty();
    }

    public TagFormats with(TagFormat format, boolean enabled) {
        EnumSet<TagFormat> copy = EnumSet.noneOf(TagFormat.class);
        copy.addAll(active);
        if (enabled) {
            copy.add(format);
        } else {
            copy.remove(format);
        }
        return new TagFormats(copy);
    }

    public List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (TagFormat format : active) {
            keys.add(format.key());
        }
        return keys;
    }

    public static TagFormats fromKeys(Collection<String> keys) {
        EnumSet<TagFormat> set = EnumSet.noneOf(TagFormat.class);
        for (String key : keys) {
            TagFormat.byKey(key.trim()).ifPresent(set::add);
        }
        return new TagFormats(set);
    }
}
