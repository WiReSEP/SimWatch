package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Instance {
    private final String
            _id,
            name,
            profile;
    /**
     * static attributes of this Instance
     */
    private JsonObject data;
    /**
     * List of all known updates in chronological order
     */
    private List<Update> updates;

    public Instance(JsonObject data, String _id, String name, String profile) {
        this.data = data;
        updates = new ArrayList<>();
        this._id = _id;
        this.name = name;
        this.profile = profile;
    }

    public Instance(JsonObject data, List<Update> updates, String _id, String name, String profile) {
        this.data = data;
        this.updates = new ArrayList<>(updates);
        this._id = _id;
        this.name = name;
        this.profile = profile;
    }

    /**
     * Retrieves a snapshot of this Instance's up-to-date data using all known updates
     *
     * @return current Snapshot
     */
    public Snapshot getSnapshot() {
        return getSnapshot(updates.size());
    }

    /**
     * Retrieves a snapshot of this Instance's up-to-date data using all the fist i known updates or
     * all updates if there are less than i known updates
     *
     * @param i Number of updates to regard
     * @return Snapshot after i-th update
     */
    public Snapshot getSnapshot(int i) {
        Snapshot snapshot = new Snapshot(profile);
        for (int j = updates.size() - 1; j >= 0; j++) {
            snapshot.addData(updates.get(j).getData());
        }
        snapshot.addData(data);
        return null;
    }

    public void addAll(List<Update> updates) {
        for (Update update : updates) {
            add(update);
        }
    }

    public void add(Update u) {
        if (!updates.contains(u)) {
            updates.add(u);
        }
    }
}
