package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Locale loader with accepted-translation filtering and source fallback. */
final class KStoryLocalization {
    private final KStoryConfig config;
    private final Map<String, String> source = new HashMap<>();
    private final Map<String, String> active = new HashMap<>();
    private final List<KStoryLocale> availableLocales;
    private final String sourceLocale;
    private String locale;

    KStoryLocalization(KStoryConfig config) {
        this.config = config;
        LocaleMetadata metadata = loadLocaleMetadata();
        this.sourceLocale = metadata.sourceLocale;
        this.availableLocales = Collections.unmodifiableList(metadata.locales);
        this.locale = sourceLocale;
        load(sourceLocale, source, false);
    }

    void setLocale(String requested) {
        locale = declaredLocale(requested);
        active.clear();
        if (!sourceLocale.equals(locale)) load(locale, active, true);
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

    String sourceLocale() { return sourceLocale; }

    List<KStoryLocale> availableLocales() { return availableLocales; }

    boolean isLocaleDeclared(String requested) {
        return findLocale(requested) != null;
    }

    private void load(String requested, Map<String, String> destination, boolean acceptedOnly) {
        FileHandle file = KStoryAssets.resolve(config, "locales/" + requested + ".json");
        if (file == null || !file.exists()) return;
        JsonValue root = new JsonReader().parse(file).get("entries");
        if (root == null || !root.isObject()) return;
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            if (!acceptedOnly || "accepted".equals(entry.getString("status", ""))) {
                destination.put(entry.name, entry.getString("text", ""));
            }
        }
    }

    private LocaleMetadata loadLocaleMetadata() {
        FileHandle file = KStoryAssets.resolve(config, "keys.json");
        if (file == null || !file.exists()) {
            warn("keys.json is missing; using the configured source locale only");
            return LocaleMetadata.sourceOnly(config.sourceLocale());
        }

        try {
            JsonValue root = new JsonReader().parse(file);
            return metadataFromJson(root, config.sourceLocale());
        } catch (Exception exception) {
            warn("Unable to read keys.json; using the configured source locale only", exception);
            return LocaleMetadata.sourceOnly(config.sourceLocale());
        }
    }

    static LocaleMetadata metadataFromJson(JsonValue root, String configuredSource) {
        String source = root == null ? configuredSource : root.getString("source_locale", configuredSource);
        if (source == null || source.trim().isEmpty()) source = configuredSource;
        List<KStoryLocale> locales = new ArrayList<>();
        locales.add(new KStoryLocale(source, true, false));

        JsonValue targets = root == null ? null : root.get("target_locales");
        if (targets != null && targets.isArray()) {
            for (JsonValue target = targets.child; target != null; target = target.next) {
                String code = target.asString();
                if (code == null || code.trim().isEmpty() || containsCode(locales, code)) continue;
                locales.add(new KStoryLocale(code, false, true));
            }
        }
        return new LocaleMetadata(source, locales);
    }

    private String declaredLocale(String requested) {
        KStoryLocale match = findLocale(requested);
        return match == null ? sourceLocale : match.code();
    }

    private KStoryLocale findLocale(String requested) {
        if (requested == null || requested.trim().isEmpty()) return null;
        String normalized = requested.trim().replace('_', '-');
        for (KStoryLocale candidate : availableLocales) {
            if (candidate.code().equalsIgnoreCase(normalized)) return candidate;
        }
        return null;
    }

    private static boolean containsCode(List<KStoryLocale> locales, String code) {
        for (KStoryLocale locale : locales) {
            if (locale.code().equalsIgnoreCase(code)) return true;
        }
        return false;
    }

    private void warn(String message) {
        if (Gdx.app != null) Gdx.app.error("KStoryLocalization", message);
    }

    private void warn(String message, Throwable error) {
        if (Gdx.app != null) Gdx.app.error("KStoryLocalization", message, error);
    }

    static final class LocaleMetadata {
        final String sourceLocale;
        final List<KStoryLocale> locales;

        LocaleMetadata(String sourceLocale, List<KStoryLocale> locales) {
            this.sourceLocale = sourceLocale;
            this.locales = locales;
        }

        static LocaleMetadata sourceOnly(String sourceLocale) {
            List<KStoryLocale> locales = new ArrayList<>();
            locales.add(new KStoryLocale(sourceLocale, true, false));
            return new LocaleMetadata(sourceLocale, locales);
        }
    }
}
