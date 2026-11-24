package de.emilo.easyclantags.storage;

import de.emilo.easyclantags.model.ClanTag;
import de.emilo.easyclantags.model.TagFormats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class TagCodec {

    private TagCodec() {
    }

    static String colorsToString(ClanTag tag) {
        List<String> parts = new ArrayList<>();
        for (int rgb : tag.colors()) {
            parts.add(String.format("%06x", rgb & 0xFFFFFF));
        }
        return String.join(",", parts);
    }

    static List<Integer> colorsFromString(String value) {
        List<Integer> colors = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return colors;
        }
        for (String part : value.split(",")) {
            colors.add(parseHex(part));
        }
        return colors;
    }

    static List<String> colorsToList(ClanTag tag) {
        List<String> parts = new ArrayList<>();
        for (int rgb : tag.colors()) {
            parts.add(String.format("%06x", rgb & 0xFFFFFF));
        }
        return parts;
    }

    static List<Integer> colorsFromList(List<String> values) {
        List<Integer> colors = new ArrayList<>();
        for (String value : values) {
            colors.add(parseHex(value));
        }
        return colors;
    }

    static String formatsToString(TagFormats formats) {
        return String.join(",", formats.keys());
    }

    static TagFormats formatsFromString(String value) {
        if (value == null || value.isBlank()) {
            return TagFormats.NONE;
        }
        return TagFormats.fromKeys(Arrays.asList(value.split(",")));
    }

    static ClanTag build(String text, List<Integer> colors, TagFormats formats) {
        if (text == null || text.isEmpty()) {
            return ClanTag.EMPTY;
        }
        return ClanTag.of(text, colors, formats, 0xFFFFFF);
    }

    private static int parseHex(String value) {
        try {
            return Integer.parseInt(value.trim().replace("#", ""), 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }
}
