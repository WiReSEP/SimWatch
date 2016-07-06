package de.tu_bs.wire.simwatch.simulation.profile;

import de.tu_bs.wire.simwatch.api.models.Profile;

/**
 * Listener to be called whenever a profile has been successfully retrieved from the server after
 * being re quested by the app
 */
public interface ProfileAcquisitionListener {

    /**
     * Called when a profile was retrieved by a ProfileProvider
     *
     * @param p The profile that was acquired
     */
    void onProfileAcquired(Profile p);

}
