package de.tu_bs.wire.simwatch.ui;

import android.content.Context;
import android.widget.ListView;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.simulation.ViewMemory;

/**
 * Can build a Menu filled with Instances and later tell which MenuItem id belongs to which
 * instance
 */
public class MenuCreator {

    private static final String TAG = "MenuCreator";
    private Map<Long, Instance> map;
    private Context context;

    public MenuCreator(Context context) {
        this.context = context;
        map = new HashMap<>();
    }

    /**
     * Puts an item into the given menu for each given Instance and remembers the MenuItems added
     * until this method is called again
     *
     * @param listView  The menu to populate
     * @param instances The Instances to populate the list with
     */
    public void populateSimulationList(ListView listView, Collection<Instance> instances, ViewMemory viewMemory) {
        map.clear();

        NavigationItem[] entries = new NavigationItem[instances.size()];

        Map<NavigationItem, Instance> item2instance = new HashMap<>();
        int counter = 0;
        for (Instance instance : instances) {
            entries[counter] = getMenuEntry(instance, viewMemory);
            item2instance.put(entries[counter], instance);
            counter++;
        }
        Arrays.sort(entries, new NavigationSorter());
        for (int i = 0; i < entries.length; i++) {
            map.put((long) i, item2instance.get(entries[i]));
        }
        listView.setAdapter(new NavigationAdapter(context, R.layout.nav_item, entries));
    }

    private NavigationItem getMenuEntry(Instance instance, ViewMemory viewMemory) {
        int notViewed = viewMemory.getNotViewed(instance);
        if (instance.getLastUpdate() == null) {
            return new NavigationItem(instance.getName(), instance.getID(), notViewed, instance.getDateOfCreation(), null, instance.getStatus());
        } else {
            return new NavigationItem(instance.getName(), instance.getID(), notViewed, instance.getDateOfCreation(), instance.getLastUpdate().getDateOfCreation(), instance.getStatus());
        }
    }

    /**
     * Retrieves the Instance that corresponds to the given MenuItem in the Menu most recently
     * populated by this MenuCreator
     *
     * @param position The position of the list item to search for
     * @return The corresponding Instance or null, if the given MenuItem does not belong to the
     * MenuItems created by populateSimulationList
     */
    public Instance getInstanceChosen(long position) {
        return map.get(position);
    }

    /**
     * Checks if the given MenuItem was created by populateSimulationList
     *
     * @param position The item in question
     * @return true, if the item was created by populateSimulationList or false otherwise
     */
    public boolean hasItem(long position) {
        return map.containsKey(position);
    }

}
