package de.tu_bs.wire.simwatch.net;

import java.util.Collection;
import java.util.List;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.simulation.UpdateListener;

/**
 * Provides access to Updates for Instances
 */
public abstract class UpdateProvider {

    protected UpdateListener listener;

    UpdateProvider(UpdateListener listener) {
        this.listener = listener;
    }

    public void setListener(UpdateListener listener) {
        this.listener = listener;
    }

    public abstract void update(Instance sim);

    public abstract void update(Collection<Instance> sims);

    protected boolean applyUpdates(Instance simulation, List<Update> updates) {
        return simulation.addAll(updates);
    }

}
