package de.tu_bs.wire.simwatch.simulation;

import java.util.Map;

/**
 * Created by mw on 22.03.16.
 */
abstract public class ViewMemoryStorage {

    public abstract Map<String, Integer> readStorage();

    public abstract void writeStorage(Map<String, Integer> storage);
}
