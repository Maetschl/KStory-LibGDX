package com.maetschl.kstory.libgdx;

/** Result returned by a game adapter action. */
public final class KStoryActionResult {
    public final boolean success;
    public final boolean supported;
    public final String message;

    private KStoryActionResult(boolean success, boolean supported, String message) {
        this.success = success;
        this.supported = supported;
        this.message = message;
    }

    public static KStoryActionResult ok(String message) { return new KStoryActionResult(true, true, message); }
    public static KStoryActionResult unsupported(String message) { return new KStoryActionResult(false, false, message); }
    public static KStoryActionResult error(String message) { return new KStoryActionResult(false, true, message); }
}
