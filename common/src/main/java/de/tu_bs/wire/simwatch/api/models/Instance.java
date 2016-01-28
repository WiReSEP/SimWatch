package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Instance {
    @SerializedName("_id")
    private String ID;
    private String name;
    @SerializedName("profile_id")
    private String profileID;
    private Profile profile;

    /**
     * List of all known updates in chronological order
     */
    private List<Update> updates;

    public Instance(String name, Profile profile) {
        this.name = name;
        this.profile = profile;
    }

    public Instance(JsonObject data, String ID, String name, String profileID) {
        updates = new ArrayList<>();
        this.ID = ID;
        this.name = name;
        this.profileID = profileID;
    }

    public Instance(JsonObject data, List<Update> updates, String ID, String name, String profileID) {
        this.updates = new ArrayList<>(updates);
        this.ID = ID;
        this.name = name;
        this.profileID = profileID;
    }

    public static Instance fromString(String str) {
        return new Gson().fromJson(str, Instance.class);
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
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
        Snapshot snapshot = new Snapshot(profileID);
        for (int j = updates.size() - 1; j >= 0; j++) {
            snapshot.addData(updates.get(j).getData());
        }
        return null;
    }

    /**
     * Adds all updates given to this simulation in order. Duplicate updates, will be omitted. The
     * addition of subsequent, non-duplicate Updates is unaffected
     *
     * @param updates Updates to add in order
     * @return true, if any of the updates were new, false otherwise
     */
    public boolean addAll(List<Update> updates) {
        boolean changeHappened = false;
        for (Update update : updates) {
            if (add(update)) {
                changeHappened = true;
            }
        }
        return changeHappened;
    }

    public int getNumberOfUpdates() {
        return updates.size();
    }

    public boolean add(Update u) {
        if (!updates.contains(u)) {
            updates.add(u);
            return true;
        } else {
            return false;
        }
    }
}
