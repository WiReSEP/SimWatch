package de.tu_bs.wire.simwatch.simulation.profile;

import java.util.Collection;

import de.tu_bs.wire.simwatch.api.models.Profile;

/**
 * Interface for a local storage that can persistently save Profiles beyond the lifecycle of the
 * application
 */
public interface ProfileStorage {

    /**
     * Writes the given Profile into the storage. If the storage already hold a Profile with the
     * same id the original Profile will be overridden by the new Profile
     *
     * @param profile The Profile to be saved
     */
    void writeProfile(Profile profile);

    /**
     * Retrieves a single Profile identified by the given id from the local storage
     *
     * @param id The id of the requested Profile
     * @return The Profile with the given id or null, if the local storage holds no such Profile
     */
    Profile readProfile(String id);

    /**
     * Reads and returns all Profiles that the storage currently has saved
     *
     * @return A Collection of all profiles currently saved
     */
    Collection<Profile> readAllInstances();

    /**
     * Removes a single Profile identified by the given id from the local storage, or does nothing
     * if the storage contains no such Profile
     *
     * @param id The id of the Profile to be deleted
     * @return true, if the storage was changed by this method call, or false otherwise
     */
    boolean deleteProfile(String id);
}
