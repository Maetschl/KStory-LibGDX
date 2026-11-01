package com.maetschl.kstory.libgdx;

import java.util.Collections;
import java.util.List;

/** Read-only view of the current node. */
public final class KStoryStep {
    public final String storyId;
    public final String nodeId;
    public final String nodeType;
    public final String dialogueId;
    public final String dialogueLineId;
    public final String questId;
    public final String textKey;
    public final KStoryAction action;
    public final List<String> outputs;

    KStoryStep(String storyId, String nodeId, String nodeType, String dialogueId,
               String dialogueLineId, String questId, String textKey,
               KStoryAction action, List<String> outputs) {
        this.storyId = storyId;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.dialogueId = dialogueId;
        this.dialogueLineId = dialogueLineId;
        this.questId = questId;
        this.textKey = textKey;
        this.action = action;
        this.outputs = Collections.unmodifiableList(outputs);
    }
}
