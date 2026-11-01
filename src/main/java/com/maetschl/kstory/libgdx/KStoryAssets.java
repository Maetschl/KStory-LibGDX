package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/** Shared asset resolution for packaged games and Gradle/headless layouts. */
final class KStoryAssets {
    private KStoryAssets() {}

    static FileHandle resolve(KStoryConfig config, String name) {
        if (Gdx.files == null) return null;
        FileHandle packaged = Gdx.files.internal(config.assetRoot() + name);
        if (packaged.exists()) return packaged;
        FileHandle nested = Gdx.files.internal("assets/" + config.assetRoot() + name);
        if (nested.exists()) return nested;
        return Gdx.files.internal("core/assets/" + config.assetRoot() + name);
    }
}
