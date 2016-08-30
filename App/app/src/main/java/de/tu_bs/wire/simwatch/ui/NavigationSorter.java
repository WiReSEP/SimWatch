package de.tu_bs.wire.simwatch.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mw on 19.08.16.
 */
public class NavigationSorter implements Comparator<NavigationItem> {

    /**
     * Sorts NavigationItems by all criteria in this list, where the first criterium in the list is
     * the most important
     */
    private final List<SortElement> sortOrder = new ArrayList<>();

    public NavigationSorter() {
        addSort(SortElement.NEW_UPDATES_DESC);
        addSort(SortElement.NAME_ASC);
    }

    public NavigationSorter(List<SortElement> sortOrder) {
        setSortOrder(sortOrder);
    }

    private static <T extends Comparable<T>> int checkNullAndCompare(T a, T b) {
        if (a == null) {
            if (b == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (b == null) {
                return -1;
            } else {
                return a.compareTo(b);
            }
        }
    }

    public void setSortOrder(List<SortElement> list) {
        sortOrder.clear();
        sortOrder.addAll(list);
    }

    public void clearSortOrder() {
        sortOrder.clear();
    }

    public void addSort(SortElement sortElement) {
        sortOrder.add(sortElement);
    }

    @Override
    synchronized public int compare(NavigationItem item1, NavigationItem item2) {
        for (int i = 0; i < sortOrder.size(); i++) {
            int comp = compareBy(item1, item2, sortOrder.get(i));
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }

    private int compareBy(NavigationItem item1, NavigationItem item2, SortElement element) {
        switch (element) {
            case NAME_ASC:
                return checkNullAndCompare(item1.getName(), item2.getName());
            case NAME_DESC:
                return compareBy(item2, item1, SortElement.NAME_ASC);
            case UUID_ASC:
                return checkNullAndCompare(item1.getUUID(), item2.getUUID());
            case UUID_DESC:
                return compareBy(item2, item1, SortElement.UUID_ASC);
            case NEW_UPDATES_ASC:
                return checkNullAndCompare(item1.getNewUpdates(), item2.getNewUpdates());
            case NEW_UPDATES_DESC:
                return compareBy(item2, item1, SortElement.NEW_UPDATES_ASC);
            case DATE_OF_CREATION_ASC:
                return checkNullAndCompare(item1.getDateOfCreation(), item2.getDateOfCreation());
            case DATE_OF_CREATION_DESC:
                return compareBy(item2, item1, SortElement.DATE_OF_CREATION_ASC);
            case LAST_UPDATE_ASC:
                return checkNullAndCompare(item1.getLastUpdate(), item2.getLastUpdate());
            case LAST_UPDATE_DESC:
                return compareBy(item2, item1, SortElement.LAST_UPDATE_ASC);
            case STATUS_ASC:
                return checkNullAndCompare(item1.getStatus(), item2.getStatus());
            case STATUS_DESC:
                return compareBy(item2, item1, SortElement.STATUS_ASC);
            default:
                return 0;
        }
    }

    public enum SortElement {NAME_ASC, NAME_DESC, UUID_ASC, UUID_DESC, NEW_UPDATES_ASC, NEW_UPDATES_DESC, DATE_OF_CREATION_ASC, DATE_OF_CREATION_DESC, LAST_UPDATE_ASC, LAST_UPDATE_DESC, STATUS_ASC, STATUS_DESC}
}
