package com.maetschl.kstory.libgdx;

/**
 * Game-owned boundary for effects and conditions. KStory never imports a
 * game's inventory, quest, scene, entity or UI classes.
 */
public interface KStoryGameAdapter {
    default KStoryActionResult execute(KStoryAction action) {
        return KStoryActionResult.unsupported(action == null ? "null" : action.type());
    }

    default boolean evaluate(KStoryCondition condition) { return condition == null || condition.isAlways(); }
}
