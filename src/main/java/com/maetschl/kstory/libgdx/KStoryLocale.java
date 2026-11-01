package com.maetschl.kstory.libgdx;

import java.util.Locale;

/** Immutable locale metadata exposed by the KStory runtime. */
public final class KStoryLocale {
    private final String code;
    private final String displayName;
    private final boolean source;
    private final boolean target;

    KStoryLocale(String code, boolean source, boolean target) {
        this.code = code;
        this.displayName = displayNameFor(code);
        this.source = source;
        this.target = target;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isSource() {
        return source;
    }

    public boolean isTarget() {
        return target;
    }

    @Override
    public String toString() {
        return displayName + " (" + code + ")";
    }

    private static String displayNameFor(String code) {
        Locale locale = Locale.forLanguageTag(code);
        String language = locale.getLanguage();
        if (language.isEmpty()) return code;

        String display = locale.getDisplayLanguage(locale);
        if (display == null || display.isEmpty()) return code;
        if (display.equalsIgnoreCase(language)) return code;
        return Character.toUpperCase(display.charAt(0)) + display.substring(1);
    }
}
