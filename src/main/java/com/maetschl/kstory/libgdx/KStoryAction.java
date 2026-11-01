package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.utils.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Typed-at-runtime action payload from a schema 1 node. */
public final class KStoryAction {
    private final String type;
    private final Map<String, String> parameters;
    private final String callbackId;

    private KStoryAction(String type, Map<String, String> parameters, String callbackId) {
        this.type = type == null ? "" : type;
        this.parameters = Collections.unmodifiableMap(parameters);
        this.callbackId = callbackId;
    }

    public static KStoryAction fromJson(String type, JsonValue value) {
        Map<String, String> parameters = new LinkedHashMap<>();
        String callbackId = null;
        if (value != null && value.isObject()) {
            callbackId = value.getString("callback_id", null);
            JsonValue data = value.get("data");
            JsonValue source = data != null && data.isObject() ? data : value;
            callbackId = source.getString("callback_id", callbackId);
            JsonValue params = value.get("params");
            if (params == null && data != null && data.isObject()) params = data;
            if (params == null && value.isObject()) params = value;
            if (params != null && params.isObject()) {
                for (JsonValue item = params.child; item != null; item = item.next) {
                    if (!"callback_id".equals(item.name)) parameters.put(item.name, item.asString());
                }
            }
        }
        return new KStoryAction(type, parameters, callbackId);
    }

    public String type() { return type; }
    public Map<String, String> parameters() { return parameters; }
    public String parameter(String name) { return parameters.get(name); }
    public String callbackId() { return callbackId; }
}
