package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

/** Locale loader with accepted-translation filtering and source fallback. */
final class KStoryLocalization {
    private final KStoryConfig config;
    private final Map<String, String> source = new HashMap<>();
    private final Map<String, String> active = new HashMap<>();
    private String locale;

    KStoryLocalization(KStoryConfig config) {
        this.config = config;
        this.locale = config.sourceLocale();
        load(config.sourceLocale(), source, false);
    }

    void setLocale(String requested) {
        locale = requested == null || requested.isEmpty() ? config.sourceLocale() : requested.toLowerCase();
        active.clear();
        if (!config.sourceLocale().equals(locale)) load(locale, active, true);
    }

    String translate(String key, Map<String, String> variables) {
        String value = active.containsKey(key) ? active.get(key) : source.get(key);
        if (value == null) return "[" + key + "]";
        if (variables != null) {
            for (Map.Entry<String, String> variable : variables.entrySet()) {
                value = value.replace("{" + variable.getKey() + "}", variable.getValue());
                value = value.replace("${" + variable.getKey() + "}", variable.getValue());
            }
        }
        return value;
    }

    String locale() { return locale; }

    private void load(String requested, Map<String, String> destination, boolean acceptedOnly) {
        if (Gdx.files == null) return;
        FileHandle file = Gdx.files.internal(config.assetRoot() + "locales/" + requested + ".json");
        if (!file.exists()) return;
        JsonValue root = new JsonReader().parse(file).get("entries");
        if (root == null || !root.isObject()) return;
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            if (!acceptedOnly || "accepted".equals(entry.getString("status", ""))) {
                destination.put(entry.name, entry.getString("text", ""));
            }
        }
    }
}
