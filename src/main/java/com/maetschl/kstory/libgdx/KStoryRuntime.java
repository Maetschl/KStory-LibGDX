package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic LibGDX runtime for KStory content schema 1. It owns story cursors
 * and localization, while the host game owns gameplay effects through its
 * {@link KStoryGameAdapter}.
 */
public final class KStoryRuntime {
    private final KStoryConfig config;
    private final KStoryGameAdapter adapter;
    private final KStoryLocalization localization;
    private final Map<String, StoryDefinition> stories = new LinkedHashMap<>();
    private final Map<String, KStoryInstance> instances = new HashMap<>();
    private KStoryInstance active;

    private KStoryRuntime(KStoryConfig config, KStoryGameAdapter adapter) {
        this.config = config;
        this.adapter = adapter == null ? new KStoryGameAdapter() {} : adapter;
        this.localization = new KStoryLocalization(config);
        load();
    }

    public static KStoryRuntime create(KStoryConfig config, KStoryGameAdapter adapter) {
        if (config == null) config = KStoryConfig.builder().build();
        return new KStoryRuntime(config, adapter);
    }

    public void load() {
        stories.clear();
        FileHandle file = file("stories.json");
        if (!file.exists()) return;
        JsonValue values = new JsonReader().parse(file).get("stories");
        if (values == null || !values.isObject()) return;
        for (JsonValue story = values.child; story != null; story = story.next) {
            stories.put(story.name, StoryDefinition.read(story.name, story));
        }
    }

    public void setLocale(String locale) { localization.setLocale(locale); }
    public String locale() { return localization.locale(); }
    public String translate(String key, Map<String, String> variables) { return localization.translate(key, variables); }
    public String translate(String key) { return translate(key, null); }

    public KStoryInstance startStory(String storyId) {
        StoryDefinition story = stories.get(storyId);
        if (story == null || story.entryNodeId.isEmpty()) return null;
        active = new KStoryInstance(storyId, story.entryNodeId);
        instances.put(storyId, active);
        executeCurrentNode();
        return active;
    }

    public KStoryStep currentStep() {
        if (active == null) return null;
        StoryDefinition story = stories.get(active.storyId());
        Node node = story == null ? null : story.nodes.get(active.currentNodeId());
        return node == null ? null : node.step(active.storyId(), active.currentNodeId());
    }

    public KStoryStep advance() {
        return moveToOutput("default");
    }

    public KStoryStep choose(String outputId) {
        return moveToOutput(outputId == null ? "default" : outputId);
    }

    public boolean evaluate(KStoryCondition condition) { return adapter.evaluate(condition); }
    public KStoryActionResult execute(KStoryAction action) { return adapter.execute(action); }
    public boolean hasStory(String storyId) { return stories.containsKey(storyId); }
    public List<String> storyIds() { return new ArrayList<>(stories.keySet()); }

    private KStoryStep moveToOutput(String outputId) {
        if (active == null) return null;
        StoryDefinition story = stories.get(active.storyId());
        Node current = story == null ? null : story.nodes.get(active.currentNodeId());
        if (current == null) return null;
        Edge edge = current.edge(outputId);
        if (edge == null && "default".equals(outputId) && !current.edges.isEmpty()) edge = current.edges.get(0);
        if (edge == null) return currentStep();
        if (edge.condition != null && !evaluate(edge.condition)) return currentStep();
        if (story.nodes.get(edge.targetNodeId) == null) return currentStep();
        active.moveTo(edge.targetNodeId);
        executeCurrentNode();
        return currentStep();
    }

    private void executeCurrentNode() {
        KStoryStep step = currentStep();
        if (step != null && step.action != null) execute(step.action);
    }

    private FileHandle file(String name) {
        FileHandle packaged = Gdx.files.internal(config.assetRoot() + name);
        if (packaged.exists()) return packaged;
        FileHandle nested = Gdx.files.internal("assets/" + config.assetRoot() + name);
        if (nested.exists()) return nested;
        return Gdx.files.internal("core/assets/" + config.assetRoot() + name);
    }

    private static final class StoryDefinition {
        final String id;
        final String entryNodeId;
        final Map<String, Node> nodes = new LinkedHashMap<>();

        private StoryDefinition(String id, String entryNodeId) {
            this.id = id;
            this.entryNodeId = entryNodeId;
        }

        static StoryDefinition read(String id, JsonValue value) {
            StoryDefinition story = new StoryDefinition(id, value.getString("entry_node_id", ""));
            JsonValue nodes = value.get("nodes");
            if (nodes != null && nodes.isObject()) {
                for (JsonValue node = nodes.child; node != null; node = node.next) {
                    story.nodes.put(node.name, Node.read(node.name, node));
                }
            }
            return story;
        }
    }

    private static final class Node {
        final String id;
        final String type;
        final String dialogueId;
        final String dialogueLineId;
        final String questId;
        final String textKey;
        final KStoryAction action;
        final List<Edge> edges = new ArrayList<>();

        private Node(String id, String type, String dialogueId, String dialogueLineId,
                     String questId, String textKey, KStoryAction action) {
            this.id = id;
            this.type = type;
            this.dialogueId = dialogueId;
            this.dialogueLineId = dialogueLineId;
            this.questId = questId;
            this.textKey = textKey;
            this.action = action;
        }

        static Node read(String id, JsonValue value) {
            String type = value.getString("node_type", "end");
            String dialogueId = value.getString("dialogue_id", null);
            String lineId = value.getString("dialogue_line_id", null);
            String questId = value.getString("quest_id", null);
            String textKey = value.getString("key", null);
            KStoryAction action = null;
            JsonValue payload = value.get("payload");
            if (payload != null) {
                String payloadType = payload.getString("type", type);
                JsonValue data = payload.get("data");
                JsonValue source = data != null && data.isObject() ? data : payload;
                if ("dialogue".equals(payloadType)) {
                    dialogueId = source.getString("dialogue_id", dialogueId);
                    lineId = source.getString("line_id", lineId);
                } else if (!"end".equals(payloadType) && !"choice".equals(payloadType)
                        && !"condition".equals(payloadType)) {
                    action = KStoryAction.fromJson(
                            "action".equals(payloadType) ? source.getString("type", "") : payloadType,
                            source);
                }
            }
            if (action == null && value.get("action") != null) {
                JsonValue legacy = value.get("action");
                action = KStoryAction.fromJson(legacy.getString("action_type", legacy.getString("type", "")), legacy);
            }
            Node node = new Node(id, type, dialogueId, lineId, questId, textKey, action);
            JsonValue next = value.get("next");
            if (next != null && next.isArray()) {
                int ordinal = 0;
                for (JsonValue target = next.child; target != null; target = target.next) {
                    node.edges.add(new Edge("default" + (ordinal++), target.asString(), "default", null));
                }
            }
            JsonValue choices = value.get("choices");
            if (choices != null && choices.isArray()) {
                for (JsonValue choice = choices.child; choice != null; choice = choice.next) {
                    node.edges.add(new Edge(
                            choice.getString("id", choice.getString("output_id", "default")),
                            choice.getString("next", choice.getString("target_node_id", "")),
                            choice.getString("output_id", choice.getString("id", "default")),
                            choice.get("condition") == null ? null : KStoryCondition.fromJson(choice.get("condition"))));
                }
            }
            JsonValue connections = value.get("connections");
            if (connections != null && connections.isArray()) {
                for (JsonValue connection = connections.child; connection != null; connection = connection.next) {
                    node.edges.add(new Edge(
                            connection.getString("id", ""),
                            connection.getString("to_node_id", ""),
                            connection.getString("output_id", "default"),
                            connection.get("condition") == null ? null : KStoryCondition.fromJson(connection.get("condition"))));
                }
            }
            return node;
        }

        Edge edge(String outputId) {
            for (Edge edge : edges) if (outputId.equals(edge.outputId)) return edge;
            return null;
        }

        KStoryStep step(String storyId, String nodeId) {
            List<String> outputs = new ArrayList<>();
            for (Edge edge : edges) outputs.add(edge.outputId);
            return new KStoryStep(storyId, nodeId, type, dialogueId, dialogueLineId, questId, textKey, action, outputs);
        }
    }

    private static final class Edge {
        final String id;
        final String targetNodeId;
        final String outputId;
        final KStoryCondition condition;

        Edge(String id, String targetNodeId, String outputId, KStoryCondition condition) {
            this.id = id;
            this.targetNodeId = targetNodeId;
            this.outputId = outputId;
            this.condition = condition;
        }
    }
}
