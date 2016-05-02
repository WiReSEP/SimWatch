package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.types.Types;

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
        //fixme inconsistent initialization; ID? profileID?
    }

    public Instance(String ID, String name, String profileID) {
        updates = new ArrayList<>();
        this.ID = ID;
        this.name = name;
        this.profileID = profileID;
    }

    public Instance(List<Update> updates, String ID, String name, String profileID) {
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
        return getSnapshot(getNumberOfUpdates());
    }

    /**
     * Retrieves a snapshot of this Instance's up-to-date data using all the fist i known updates or
     * all updates if there are less than i known updates
     *
     * @param i Number of updates to regard
     * @return Snapshot after i-th update
     */
    public Snapshot getSnapshot(int i) {
        Snapshot snapshot = new Snapshot(ID, name, profileID);
        for (int j = 0; j < i && j < getNumberOfUpdates(); j++) {
            snapshot.addUpdate(updates.get(j));
        }
        return snapshot;
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
        if (updates == null) {
            return 0;
        } else {
            return updates.size();
        }
    }

    public String getLastOccurrence(String attachmentName) {
        String updateID = null;
        for (int i = updates.size() - 1; i >= 0; i--) {
            Update update = updates.get(i);
            if (update.attachments.contains(attachmentName)) {
                updateID = update.getID();
                break;
            }
        }
        return updateID;
    }

    public String getProfileID() {
        return profileID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instance)) return false;

        Instance instance = (Instance) o;

        return ID != null ? ID.equals(instance.ID) : instance.ID == null;

    }

    public Collection<Attachment> getAttachments() {
        Collection<Attachment> attachments = new ArrayList<>();
        Map<String, String> properties = getProfile().getProperties();
        for (String propertyName : properties.keySet()) {
            Types.Type propertyType = Types.getType(properties.get(propertyName));
            switch (propertyType) {
                case IMAGE_BINARY:
                case NON_IMAGE_BINARY:
                    attachments.add(new Attachment(getID(), propertyName));
                    break;
                default:
            }
        }
        return attachments;
    }

    public boolean add(Update u) {
        if (updates == null) {
            updates = new ArrayList<>();
        }
        if (!updates.contains(u)) {
            updates.add(u);
            return true;
        } else {
            return false;
        }
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Update> getUpdates() {
        if (updates == null) {
            return new ArrayList<>();
        } else {
            return updates;
        }
    }

}
