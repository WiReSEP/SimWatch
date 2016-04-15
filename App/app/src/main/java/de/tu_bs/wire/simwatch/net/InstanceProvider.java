package de.tu_bs.wire.simwatch.net;

import de.tu_bs.wire.simwatch.simulation.InstanceAcquisitionListener;

/**
 * Provides access to new Instances or a list of Instance IDs
 */
public abstract class InstanceProvider {

    protected InstanceAcquisitionListener listener;

    public InstanceProvider(InstanceAcquisitionListener listener) {
        this.listener = listener;
    }

    public abstract void getNewInstance(String id);

    public abstract void getNewInstances(String[] id);

    public abstract void deleteInstance(String id);

    public abstract void getAvailableInstances();
}
