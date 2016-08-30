package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.GsonUtil;
import de.tu_bs.wire.simwatch.api.HashUtil;
import de.tu_bs.wire.simwatch.api.types.Types;

public class Instance {
    @SerializedName("_id")
    private String ID;
    private String name;
    @SerializedName("profile_id")
    private String profileID;
    private Profile profile;
    private Status status;
    private Error error;
    /**
     * List of all known updates in chronological order
     */
    private List<Update> updates;

    @SerializedName("date")
    private Date dateOfCreation;

    public Instance(String name, Profile profile) {
        this.name = name;
        this.profile = profile;
        // unregistered instance, there is no ID or profileID at this point
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
        try {
            return GsonUtil.getGson().fromJson(str, Instance.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }

    /**
     * Calculates a 5 character hash of the id
     *
     * @return a 20-bit hex hash of the id (5 characters)
     */
    public String getShortenedID() {
        return HashUtil.shorten(ID);
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
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

    public Update getLastUpdate() {
        if (getNumberOfUpdates() > 0) {
            return getUpdates().get(getNumberOfUpdates() - 1);
        } else {
            return null;
        }
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public boolean applyStatus(InstanceStatus instanceStatus) {
        if (status != null && !status.equals(instanceStatus.getStatus()) || error != null && !error.equals(instanceStatus.getError())) {
            status = instanceStatus.getStatus();
            error = instanceStatus.getError();
            return true;
        } else {
            return false;
        }
    }

    public enum Status {
        /**
         * Simulation is running normally
         */
        RUNNING,
        /**
         * Simulation is completed and shut down
         */
        STOPPED,
        /**
         * Simulation crashed or failed for other reasons
         *
         * @see Error
         */
        FAILED
    }

    /**
     * Represents an unrecoverable error that occurred during the execution of
     * the simulation instance.
     */
    public static class Error {
        private String message;

        private String stackTrace;


        public Error(String message, Throwable throwable) {
            this.message = message;
            if (throwable != null) {
                this.stackTrace = getStackTrace(throwable);
            }
        }

        private static String getStackTrace(Throwable throwable) {
            StringWriter stringWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Error)) return false;

            Error error = (Error) o;

            return message != null ? message.equals(error.message) : error.message == null && (stackTrace != null ? stackTrace.equals(error.stackTrace) : error.stackTrace == null);
        }

        @Override
        public int hashCode() {
            int result = message != null ? message.hashCode() : 0;
            result = 31 * result + (stackTrace != null ? stackTrace.hashCode() : 0);
            return result;
        }

        /**
         * User-specified error message
         *
         * @return Error message. May be <code>null</code>
         */
        public String getMessage() {
            return message;
        }

        /**
         * If the user supplied a <code>Throwable</code>, this method
         * returns the entire stack trace as string.
         *
         * @return Stack trace if there is one or <code>null</code>
         * @see Throwable#printStackTrace()
         */
        public String getStackTrace() {
            return stackTrace;
        }

    }
}
