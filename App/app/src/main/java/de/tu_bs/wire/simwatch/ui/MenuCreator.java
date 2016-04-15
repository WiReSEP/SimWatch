package de.tu_bs.wire.simwatch.ui;

import android.view.Menu;
import android.view.MenuItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.simulation.ViewMemory;

/**
 * Can build a Menu filled with Instances and later tell which MenuItem id belongs to which
 * instance
 */
public class MenuCreator {

    private static final String TAG = "MenuCreator";
    private Map<MenuItem, Instance> map;

    public MenuCreator() {
        map = new HashMap<>();
    }

    /**
     * Puts an item into the given menu for each given Instance and remembers the MenuItems added
     * until this method is called again
     *
     * @param menu      The menu to populate
     * @param instances The Instances to populate the Menu with
     */
    public void populateSimulationMenu(Menu menu, Collection<Instance> instances, ViewMemory viewMemory) {
        map.clear();
        menu.clear();

        for (Instance instance : instances) {
            MenuItem menuItem = menu.add(getMenuEntry(instance, viewMemory));
            map.put(menuItem, instance);
        }
    }

    private String getMenuEntry(Instance instance, ViewMemory viewMemory) {
        int notViewed = viewMemory.getNotViewed(instance);
        String entry;
        if (notViewed > 0) {
            entry = String.format(Locale.getDefault(), "%s (%d)", instance.getName(), notViewed);
        } else {
            entry = instance.getName();
        }
        return entry;
    }

    /**
     * Retrieves the Instance that corresponds to the given MenuItem in the Menu most recently
     * populated by this MenuCreator
     *
     * @param menuItem The MenuItem to search for
     * @return The corresponding Instance or null, if the given MenuItem does not belong to the
     * MenuItems created by populateSimulationMenu
     */
    public Instance getInstanceChosen(MenuItem menuItem) {
        return map.get(menuItem);
    }

    /**
     * Checks if the given MenuItem was created by populateSimulationMenu
     *
     * @param menuItem The item in question
     * @return true, if the item was created by populateSimulationMenu or false otherwise
     */
    public boolean hasItem(MenuItem menuItem) {
        return map.containsKey(menuItem);
    }

}
