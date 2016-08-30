package de.tu_bs.wire.simwatch.simulation.profile;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.net.HTTPProfileProvider;

/**
 * Stores Profiles to access via their IDs
 */
public class ProfileManager implements ProfileAcquisitionListener {

    static ProfileManager instance;
    private final Map<String, Profile> profiles;
    private final ProfileStorage storage;
    private final Context context;

    private ProfileManager(Context context) {
        this.context = context;
        profiles = new HashMap<>();
        storage = new FileProfileStorage(context);
        readAllProfiles();
    }

    synchronized public static ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context);
        }
        return instance;
    }

    public static ProfileManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ProfileManager not initialized");
        }
        return instance;
    }

    public void readAllProfiles() {
        Collection<Profile> profiles = storage.readAllProfiles();
        synchronized (this.profiles) {
            for (Profile profile : profiles) {
                this.profiles.put(profile.getID(), profile);
            }
        }
    }

    public boolean has(String id) {
        synchronized (profiles) {
            return profiles.containsKey(id);
        }
    }

    public void addProfile(Profile p) {
        if (p == null) {
            throw new NullPointerException("profile is null");
        }
        synchronized (profiles) {
            if (!profiles.containsKey(p.getID())) {
                profiles.put(p.getID(), p);
            }
        }
        storage.writeProfile(p);
    }

    public void haveProfiles(Collection<String> IDs) {
        for (String id : IDs) {
            if (!has(id)) {
                acquireProfile(id);
            }
        }
        removeOldProfiles(IDs);
    }

    /**
     * Removes all Profiles from this manager except those specified in the given Collection
     *
     * @param IDs The IDs of the Profiles to keep
     */
    private void removeOldProfiles(Collection<String> IDs) {
        Collection<String> profilesToBeRemoved = new ArrayList<>();
        synchronized (profiles) {
            for (String id : profiles.keySet()) {
                if (!IDs.contains(id)) {
                    profilesToBeRemoved.add(id);
                }
            }
        }
        for (String id : profilesToBeRemoved) {
            removeProfile(id);
        }
    }

    public void acquireProfile(String ID) {
        new HTTPProfileProvider(this, context).acquireProfile(ID);
    }

    public Profile getProfile(String id) {
        synchronized (profiles) {
            return profiles.get(id);
        }
    }

    public void removeProfile(String id) {
        synchronized (profiles) {
            profiles.remove(id);
        }
        storage.deleteProfile(id);
    }

    @Override
    public void onProfileAcquired(Profile p) {
        addProfile(p);
    }
}
