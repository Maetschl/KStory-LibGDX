# KStory-LibGDX

Generic Java/Gradle runtime for KStory content schema `1`, maintained under the
GitHub user `Maetschl`.

The runtime is game-agnostic. It does not import game-specific items, quests,
scenes, notifications or entities. A game supplies those effects through
`KStoryGameAdapter`.

```java
KStoryRuntime runtime = KStoryRuntime.create(
    KStoryConfig.builder()
        .assetRoot("localization")
        .sourceLocale("en")
        .build(),
    new MyGameAdapter()
);

runtime.startStory("story_id");
KStoryStep step = runtime.currentStep();
runtime.choose("output_id");
String text = runtime.translate("dialog.key", variables);
```

Locale options are declared by the project's `localization/keys.json`; the
runtime does not maintain a game-specific language list:

```java
for (KStoryLocale locale : runtime.availableLocales()) {
    System.out.println(locale.displayName() + " (" + locale.code() + ")");
}
runtime.setLocale("es");
```

`sourceLocale()` returns the declared source language. Unknown locale codes
fall back to it, and target bundles without accepted translations remain
selectable while their text falls back to the source.

The runtime reads `stories.json`, locale bundles and schema 1 content from the
configured LibGDX asset root. Only `accepted` target translations are active;
missing, pending and `on_review` entries fall back to the source locale. The
reader accepts stable connections and the legacy `next`/`choices` projection.

Build the runtime directly from this repository with:

```bash
gradle test
```

Koble consumes this project through a Gradle composite build. Clone
`Maetschl/KStory-LibGDX` next to Koble, or pass its path with
`-PkstoryLibGdxDir=/path/to/KStory-LibGDX`; Maven Central is not required.
