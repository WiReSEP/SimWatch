package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Represents the data of an Instance at the time of a given Update
 */
public class Snapshot {

    private final String profile;
    private final JsonObject data;
    private String name;
    private List<Update> updates;

    public Snapshot(String name, String profile) {
        this(name, profile, new JsonObject(), new ArrayList<Update>());
    }

    public Snapshot(String name, String profile, JsonObject data, List<Update> updates) {
        this.name = name;
        this.profile = profile;
        this.data = data;
        this.updates = updates;
    }

    public void addUpdate(Update update) {
        if (!updates.contains(update)) {
            updates.add(update);
            addData(update.getData(), true);
        }
    }

    public void addData(JsonObject newData) {
        addData(newData, false);
    }

    public void addData(JsonObject newData, boolean override) {
        for (Map.Entry<String, JsonElement> entry : newData.entrySet()) {
            if (!data.has(entry.getKey())) {
                data.add(entry.getKey(), entry.getValue());
            } else if (override) {
                data.remove(entry.getKey());
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

    public int getNumberOfUpdates() {
        return updates.size();
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public String getName() {
        return name;
    }
}
