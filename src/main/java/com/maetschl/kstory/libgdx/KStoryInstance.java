package com.maetschl.kstory.libgdx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mutable story cursor owned by a single runtime. */
public final class KStoryInstance {
    private final String storyId;
    private String currentNodeId;
    private final List<String> history = new ArrayList<>();

    KStoryInstance(String storyId, String entryNodeId) {
        this.storyId = storyId;
        this.currentNodeId = entryNodeId;
        history.add(entryNodeId);
    }

    public String storyId() { return storyId; }
    public String currentNodeId() { return currentNodeId; }
    public List<String> history() { return Collections.unmodifiableList(history); }
    void moveTo(String nodeId) { currentNodeId = nodeId; history.add(nodeId); }
}
