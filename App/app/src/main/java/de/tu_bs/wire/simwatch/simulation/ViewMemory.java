package de.tu_bs.wire.simwatch.simulation;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.models.Instance;

/**
 * Remembers, which Updates of which Instance have been viewed so far
 */
public class ViewMemory {

    private static final String TAG = "ViewMemory";
    private final Map<String, Integer> id2numViewed;
    private ViewMemoryStorage storage;

    public ViewMemory(Context context) {
        storage = new FileViewMemoryStorage(context);
        id2numViewed = storage.readStorage();
    }

    /**
     * Tells the ViewMemory that the given Instance was viewed up until the Update with the given
     * index. If update is less than 0 or if update is greater than the number of Updates of the
     * given Instance, all existing updates have been viewed instead. This method cannot be used to
     * un-view Updates. If a previous call claimed a greater number of Updates viewed, the greater
     * number is chosen instead
     *
     * @param instance    The instance that has been viewed
     * @param updateIndex The index of the most recent update
     */
    public int viewInstance(Instance instance, int updateIndex) {
        int newUpdatesViewed;

        if (updateIndex < 0) {
            updateIndex = instance.getNumberOfUpdates();
        }
        updateIndex = Math.min(instance.getNumberOfUpdates(), updateIndex);
        synchronized (id2numViewed) {
            if (id2numViewed.containsKey(instance.getID())) {
                updateIndex = Math.max(id2numViewed.get(instance.getID()), updateIndex);
                newUpdatesViewed = updateIndex - id2numViewed.get(instance.getID());
            } else {
                newUpdatesViewed = updateIndex;
            }

            id2numViewed.put(instance.getID(), updateIndex);
        }
        if (newUpdatesViewed > 0) {
            writeStorage();
        }

        Log.d(TAG, "Viewed Instance '" + instance.getID() + "' with index " + updateIndex + " (" + newUpdatesViewed + " new)");

        return newUpdatesViewed;
    }

    public boolean removeAllBut(Collection<String> instanceIDs) {
        Collection<String> instancesToBeRemoved = new ArrayList<>();
        synchronized (id2numViewed) {
            for (String id : id2numViewed.keySet()) {
                if (!instanceIDs.contains(id)) {
                    instancesToBeRemoved.add(id);
                }
            }
        }
        for (String id : instancesToBeRemoved) {
            removeInstance(id);
        }
        return !instancesToBeRemoved.isEmpty();
    }

    public boolean removeInstance(String instanceID) {
        boolean modified;
        synchronized (id2numViewed) {
            modified = (id2numViewed.remove(instanceID) != null);
        }
        writeStorage();
        return modified;
    }

    public int getViewed(String instanceID) {
        synchronized (id2numViewed) {
            if (id2numViewed.containsKey(instanceID)) {
                return id2numViewed.get(instanceID);
            } else {
                return 0;
            }
        }
    }

    public int getNotViewed(Instance instance) {
        Log.d(TAG, "Not viewed " + (instance.getNumberOfUpdates() - getViewed(instance.getID())) + " Updates of Instance '" + instance.getID() + "'");
        return instance.getNumberOfUpdates() - getViewed(instance.getID());
    }

    private void writeStorage() {
        synchronized (id2numViewed) {
            storage.writeStorage(id2numViewed);
        }
    }

}
