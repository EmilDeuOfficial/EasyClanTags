package de.emilo.easyclantags.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record ClanTag(List<TagLetter> letters, TagFormats formats) {

    public static final ClanTag EMPTY = new ClanTag(List.of(), TagFormats.NONE);

    public ClanTag {
        letters = List.copyOf(letters);
    }

    public boolean isEmpty() {
        return letters.isEmpty();
    }

    public int length() {
        return letters.size();
    }

    public String plain() {
        StringBuilder builder = new StringBuilder();
        for (TagLetter letter : letters) {
            builder.append(letter.text());
        }
        return builder.toString();
    }

    public String normalized() {
        return plain().toLowerCase(Locale.ROOT);
    }

    public List<Integer> colors() {
        List<Integer> colors = new ArrayList<>(letters.size());
        for (TagLetter letter : letters) {
            colors.add(letter.rgb());
        }
        return colors;
    }

    public ClanTag withText(String text, int defaultRgb) {
        List<TagLetter> next = new ArrayList<>();
        int[] index = {0};
        text.codePoints().forEach(codePoint -> {
            int rgb = index[0] < letters.size() ? letters.get(index[0]).rgb() : defaultRgb;
            next.add(new TagLetter(Character.toString(codePoint), rgb));
            index[0]++;
        });
        return new ClanTag(next, formats);
    }

    public ClanTag withColor(int index, int rgb) {
        if (index < 0 || index >= letters.size()) {
            return this;
        }
        List<TagLetter> next = new ArrayList<>(letters);
        next.set(index, new TagLetter(letters.get(index).text(), rgb));
        return new ClanTag(next, formats);
    }

    public ClanTag withAllColors(int rgb) {
        List<TagLetter> next = new ArrayList<>(letters.size());
        for (TagLetter letter : letters) {
            next.add(new TagLetter(letter.text(), rgb));
        }
        return new ClanTag(next, formats);
    }

    public ClanTag withColors(List<Integer> colors) {
        List<TagLetter> next = new ArrayList<>(letters.size());
        for (int i = 0; i < letters.size(); i++) {
            int rgb = i < colors.size() ? colors.get(i) : letters.get(i).rgb();
            next.add(new TagLetter(letters.get(i).text(), rgb));
        }
        return new ClanTag(next, formats);
    }

    public ClanTag withFormats(TagFormats newFormats) {
        return new ClanTag(letters, newFormats);
    }

    public static ClanTag of(String text, List<Integer> colors, TagFormats formats, int defaultRgb) {
        return new ClanTag(List.of(), formats).withText(text, defaultRgb).withColors(colors);
    }
}
