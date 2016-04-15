package de.tu_bs.wire.simwatch.simulation;

import java.util.Collection;

/**
 * Created by mw on 02.02.16.
 */
public interface GroupUpdateListener {

    void onUpdate(Collection<String> instanceIDs);

}
