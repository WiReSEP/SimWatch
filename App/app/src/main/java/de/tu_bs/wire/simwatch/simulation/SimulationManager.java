package de.tu_bs.wire.simwatch.simulation;

import android.content.Context;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.net.HTTPInstanceProvider;
import de.tu_bs.wire.simwatch.net.HTTPUpdateProvider;
import de.tu_bs.wire.simwatch.net.InstanceProvider;
import de.tu_bs.wire.simwatch.simulation.profile.ProfileManager;

/**
 * Manages the storage of Simulations on the phone
 */
public class SimulationManager implements InstanceAcquisitionListener, UpdateListener {

    private static final String TAG = "SimulationManager";

    private final Map<String, Instance> simulations;
    private final Multimap<UpdateListener, String> updateListenerRegistrations;
    private final Collection<InstanceAcquisitionListener> instanceListeners;
    private Collection<String> existingInstanceIDs;
    private InstanceProvider instanceProvider;
    private Context context;
    private ProfileManager profileManager;
    private SimulationStorage storage;
    private ViewMemory viewMemory;

    public SimulationManager(Context context, ProfileManager profileManager) throws IOException {
        this.context = context;
        this.profileManager = profileManager;
        simulations = new HashMap<>();
        storage = new FileSimulationStorage(context);
        readAllInstances();

        instanceProvider = new HTTPInstanceProvider(this, context);
        updateListenerRegistrations = HashMultimap.create();
        instanceListeners = new ArrayList<>();
        existingInstanceIDs = new ArrayList<>(simulations.keySet());
        viewMemory = new ViewMemory(context);
    }

    public ViewMemory getViewMemory() {
        return viewMemory;
    }

    public Collection<String> getInstanceList() {
        return new ArrayList<>(existingInstanceIDs);
    }

    public Collection<Instance> getInstances() {
        return simulations.values();
    }

    public void readAllInstances() {
        Collection<Instance> instances = storage.readAllInstances();
        synchronized (simulations) {
            for (Instance instance : instances) {
                simulations.put(instance.getID(), instance);
            }
        }
    }

    private void writeToFile(Instance sim) {
        if (sim != null) {
            storage.writeInstance(sim);
        }
    }

    public void deleteInstance(String instanceID) {
        instanceProvider.deleteInstance(instanceID);
    }

    @Override
    public void onInstanceListAcquired(Collection<String> instanceIDs) {
        existingInstanceIDs = new ArrayList<>(instanceIDs);
        removeOldInstances(instanceIDs);
        Collection<String> newIDs = new ArrayList<>();
        for (String instanceID : instanceIDs) {
            if (!hasInstance(instanceID)) {
                newIDs.add(instanceID);
            }
        }
        instanceProvider.getNewInstances(newIDs.toArray(new String[newIDs.size()]));
        synchronized (instanceListeners) {
            for (InstanceAcquisitionListener listener : instanceListeners) {
                listener.onInstanceListAcquired(instanceIDs);
            }
        }
    }

    @Override
    public void onInstanceListAcquisitionFailed() {
        synchronized (instanceListeners) {
            for (InstanceAcquisitionListener listener : instanceListeners) {
                listener.onInstanceListAcquisitionFailed();
            }
        }
    }

    /**
     * Removes all Instances from this SimulationManager except for those contained in the given
     * collection
     *
     * @param instancesToKeep the IDs of the Instances that are not to be deleted
     */
    private void removeOldInstances(Collection<String> instancesToKeep) {
        Collection<String> instancesToBeRemoved = new ArrayList<>();
        synchronized (simulations) {
            for (String id : simulations.keySet()) {
                if (!instancesToKeep.contains(id)) {
                    instancesToBeRemoved.add(id);
                }
            }
        }
        for (String id : instancesToBeRemoved) {
            removeInstance(id);
        }
        viewMemory.removeAllBut(instancesToKeep);
    }

    @Override
    public void onInstanceAcquired(Instance sim) {
        addInstance(sim);
        writeToFile(sim);
        synchronized (instanceListeners) {
            for (InstanceAcquisitionListener instanceListener : instanceListeners) {
                instanceListener.onInstanceAcquired(sim);
            }
        }
    }

    @Override
    public void onInstanceDeleted(String id) {
        updateAllInstances();
    }

    /**
     * Getter for Instances stored in this SimulationManager
     *
     * @param id The id of the instance to be retrieved
     * @return The corresponding Instance or null, if there is no Instance with that id
     */
    public Instance getInstance(String id) {
        if (hasInstance(id)) {
            synchronized (simulations) {
                return simulations.get(id);
            }
        } else {
            return null;
        }
    }

    /**
     * Checks if an Instance with the given id is stored in this SimulationManager
     *
     * @param id the id of the Instance
     * @return true if this SimulationManager stores an Instance with that id, false otherwise
     */
    public boolean hasInstance(String id) {
        synchronized (simulations) {
            return simulations.containsKey(id);
        }
    }

    /**
     * Adds an Instance to this SimulationManager
     *
     * @param sim The Instance to be added
     */
    private void addInstance(Instance sim) {
        boolean changeHappened = false;
        synchronized (simulations) {
            if (!hasInstance(sim.getID())) {
                changeHappened = (simulations.put(sim.getID(), sim) != sim);
            }
        }
        if (changeHappened) {
            profileManager.haveProfiles(accumulateProfiles());
        }
    }

    /**
     * Removes the Instance with the given id from this SimulationManager
     *
     * @param id The id of the Instance to be deleted
     * @return true if any changes happened, false otherwise
     */
    private boolean removeInstance(String id) {
        boolean changeHappened = false;
        synchronized (simulations) {
            if (hasInstance(id)) {
                storage.deleteInstance(id);
                Instance removedInstance = simulations.remove(id);
                changeHappened = removedInstance != null;
            }
        }
        if (changeHappened) {
            profileManager.haveProfiles(accumulateProfiles());
        }
        return changeHappened;
    }

    private Collection<String> accumulateProfiles() {
        Collection<String> profiles = new ArrayList<>();
        synchronized (simulations) {
            for (Instance instance : simulations.values()) {
                if (!profiles.contains(instance.getProfileID())) {
                    profiles.add(instance.getProfileID());
                }
            }
        }
        return profiles;
    }

    public void updateAllInstances() {
        instanceProvider.getAvailableInstances();
        profileManager.haveProfiles(accumulateProfiles());
        Collection<Instance> sims;
        synchronized (simulations) {
            sims = simulations.values();
        }
        new HTTPUpdateProvider(this, context).update(sims);
    }

    public void addInstanceAcquisitionListener(InstanceAcquisitionListener listener) {
        synchronized (instanceListeners) {
            instanceListeners.add(listener);
        }
    }

    public boolean removeInstanceAcquisitionListener(InstanceAcquisitionListener listener) {
        synchronized (instanceListeners) {
            return instanceListeners.remove(listener);
        }
    }

    public void addUpdateListener(String instanceID, UpdateListener listener) {
        synchronized (updateListenerRegistrations) {
            updateListenerRegistrations.put(listener, instanceID);
        }
    }

    public boolean removeUpdateListener(String instanceID, UpdateListener listener) {
        synchronized (updateListenerRegistrations) {
            return updateListenerRegistrations.remove(instanceID, listener);
        }
    }

    public int removeUpdateListener(UpdateListener listener) {
        Collection<String> removedInstanceIDs = updateListenerRegistrations.removeAll(listener);
        return removedInstanceIDs.size();
    }

    public void updateInstance(Instance instance) {
        new HTTPUpdateProvider(this, context).update(instance);
    }

    @Override
    public void onUpdate(Collection<String> instanceIDs) {
        for (String updatedInstanceID : instanceIDs) {
            Instance instance = getInstance(updatedInstanceID);
            if (instance != null) {
                writeToFile(instance);
            }
        }
        synchronized (updateListenerRegistrations) {
            for (UpdateListener listener : updateListenerRegistrations.keySet()) {
                Collection<String> idSubset = new ArrayList<>();
                for (String instanceID : updateListenerRegistrations.get(listener)) {
                    if (instanceIDs.contains(instanceID)) {
                        idSubset.add(instanceID);
                    }
                }
                if (idSubset.isEmpty()) {
                    listener.onNoUpdates();
                } else {
                    listener.onUpdate(idSubset);
                }
            }
        }
    }

    @Override
    public void onNoUpdates() {
        synchronized (updateListenerRegistrations) {
            for (UpdateListener listener : updateListenerRegistrations.keySet()) {
                listener.onNoUpdates();
            }
        }
    }

    @Override
    public void onUpdateFailed(Collection<String> instanceIDs) {
        synchronized (updateListenerRegistrations) {
            for (UpdateListener updateListener : updateListenerRegistrations.keySet()) {
                Collection<String> listenedForInstances = updateListenerRegistrations.get(updateListener);
                boolean listenerMatches = false;
                for (String listenedForInstance : listenedForInstances) {
                    if (instanceIDs.contains(listenedForInstance)) {
                        listenerMatches = true;
                        break;
                    }
                }
                if (listenerMatches) {
                    updateListener.onUpdateFailed(instanceIDs);
                }
            }
        }
    }
}
