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

The runtime reads `stories.json`, locale bundles and schema 1 content from the
configured LibGDX asset root. Only `accepted` target translations are active;
missing, pending and `on_review` entries fall back to the source locale. The
reader accepts stable connections and the legacy `next`/`choices` projection.

Build and publish locally with:

```bash
gradle test
gradle publishToMavenLocal
```

Releases are published from [Maetschl/KStory-LibGDX](https://github.com/Maetschl/KStory-LibGDX)
using semantic versioning and `maven-publish`.
