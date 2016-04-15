package de.tu_bs.wire.simwatch.simulation;

import java.util.Collection;

import de.tu_bs.wire.simwatch.api.models.Instance;

/**
 * Listener waiting for a new Instance or a list of Instance IDs to be downloaded
 */
public interface InstanceAcquisitionListener {

    void onInstanceListAcquired(Collection<String> instanceIDs);

    void onInstanceListAcquisitionFailed();

    void onInstanceAcquired(Instance sim);

    void onInstanceDeleted(String id);
}
