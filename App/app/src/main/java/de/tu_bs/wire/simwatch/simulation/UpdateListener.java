package de.tu_bs.wire.simwatch.simulation;

import java.util.Collection;

/**
 * Listener waiting for a new Update to be downloaded
 */
public interface UpdateListener {

    /**
     * This method is to be called after updates were acquired and applied
     *
     * @param instanceIDs The IDs of the Instances that had at least one new Update
     */
    void onUpdate(Collection<String> instanceIDs);

    void onNoUpdates();

    /**
     * This method is to be called when the UpdateProvider couldn't check for Updates for some
     * reason
     *
     * @param instanceIDs The IDs of the Instances that should have been updated, but weren't
     */
    void onUpdateFailed(Collection<String> instanceIDs);
}
