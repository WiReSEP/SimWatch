package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;


/**
 * Represents the data of an Instance at the time of a given Update
 */
public class Snapshot {
    private final String profile;

    private final JsonObject data;

    public Snapshot(String profile) {
        this(profile, new JsonObject());
    }

    public Snapshot(String profile, JsonObject data) {
        this.profile = profile;
        this.data = data;
    }

    public void addData(JsonObject newData) {
        for (Map.Entry<String, JsonElement> entry : newData.entrySet()) {
            if (!data.has(entry.getKey())) {
                data.add(entry.getKey(), entry.getValue());
            }
        }
    }

    public JsonObject getData() {
        return data;
    }

    public String getProfile() {
        return profile;
    }
}
