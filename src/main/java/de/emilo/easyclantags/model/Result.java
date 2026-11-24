package de.emilo.easyclantags.model;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.LinkedHashMap;
import java.util.Map;

public record Result(boolean ok, String key, Map<String, String> args) {

    public Result {
        args = Map.copyOf(args);
    }

    public static Result ok(String key, String... keyValues) {
        return new Result(true, key, toMap(keyValues));
    }

    public static Result error(String key, String... keyValues) {
        return new Result(false, key, toMap(keyValues));
    }

    public TagResolver[] resolvers() {
        return args.entrySet().stream()
                .map(entry -> Placeholder.unparsed(entry.getKey(), entry.getValue()))
                .toArray(TagResolver[]::new);
    }

    private static Map<String, String> toMap(String[] keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
