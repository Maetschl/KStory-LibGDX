package com.maetschl.kstory.libgdx;

/** Immutable configuration for a KStory runtime. */
public final class KStoryConfig {
    private final String assetRoot;
    private final String sourceLocale;

    private KStoryConfig(Builder builder) {
        this.assetRoot = normalizeRoot(builder.assetRoot);
        this.sourceLocale = builder.sourceLocale == null || builder.sourceLocale.isEmpty()
                ? "en" : builder.sourceLocale;
    }

    public static Builder builder() { return new Builder(); }
    public String assetRoot() { return assetRoot; }
    public String sourceLocale() { return sourceLocale; }

    private static String normalizeRoot(String root) {
        if (root == null || root.isEmpty()) return "localization/";
        return root.endsWith("/") ? root : root + "/";
    }

    public static final class Builder {
        private String assetRoot = "localization";
        private String sourceLocale = "en";
        public Builder assetRoot(String value) { assetRoot = value; return this; }
        public Builder sourceLocale(String value) { sourceLocale = value; return this; }
        public KStoryConfig build() { return new KStoryConfig(this); }
    }
}
