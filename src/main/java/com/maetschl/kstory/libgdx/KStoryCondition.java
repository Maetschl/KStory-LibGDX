package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.utils.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Condition payload evaluated by the game adapter. */
public final class KStoryCondition {
    private final String type;
    private final Map<String, String> parameters;
    private final String callbackId;

    private KStoryCondition(String type, Map<String, String> parameters, String callbackId) {
        this.type = type == null ? "always" : type;
        this.parameters = Collections.unmodifiableMap(parameters);
        this.callbackId = callbackId;
    }

    public static KStoryCondition fromJson(JsonValue value) {
        if (value == null) return new KStoryCondition("always", new LinkedHashMap<String, String>(), null);
        Map<String, String> parameters = new LinkedHashMap<>();
        JsonValue params = value.get("params");
        if (params != null && params.isObject()) {
            for (JsonValue item = params.child; item != null; item = item.next) parameters.put(item.name, item.asString());
        }
        return new KStoryCondition(value.getString("type", "always"), parameters, value.getString("callback_id", null));
    }

    public String type() { return type; }
    public Map<String, String> parameters() { return parameters; }
    public String parameter(String name) { return parameters.get(name); }
    public String callbackId() { return callbackId; }
    public boolean isAlways() { return "always".equals(type); }
}
