package de.tu_bs.wire.simwatch.net;

import de.tu_bs.wire.simwatch.simulation.profile.ProfileAcquisitionListener;

/**
 * Provides access to new Profiles
 */
public abstract class ProfileProvider {

    protected ProfileAcquisitionListener listener;

    protected ProfileProvider(ProfileAcquisitionListener listener) {
        this.listener = listener;
    }

    public void setListener(ProfileAcquisitionListener listener) {
        this.listener = listener;
    }

    public abstract void acquireProfile(String id);

}
