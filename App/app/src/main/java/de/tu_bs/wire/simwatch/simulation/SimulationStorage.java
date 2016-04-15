package de.tu_bs.wire.simwatch.simulation;

import java.util.Collection;

import de.tu_bs.wire.simwatch.api.models.Instance;

/**
 * A simple class able to write simulations into local persistent storage and read them back in a
 * later session
 */
public interface SimulationStorage {

    void writeInstance(Instance sim);

    Instance readInstance(String id);

    Collection<Instance> readAllInstances();

    boolean deleteInstance(String id);
}
