package de.tu_bs.wire.simwatch.simulation.profile;

import java.util.Collection;

import de.tu_bs.wire.simwatch.api.models.Profile;

/**
 * Created by mw on 26.02.16.
 */
public interface ProfileStorage {

    void writeProfile(Profile profile);

    Profile readProfile(String id);

    Collection<Profile> readAllInstances();

    boolean deleteProfile(String id);
}
