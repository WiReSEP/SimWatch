package de.tu_bs.wire.simwatch.ui;

import java.util.Date;

import de.tu_bs.wire.simwatch.api.HashUtil;
import de.tu_bs.wire.simwatch.api.models.Instance;

/**
 * Created by mw on 12.07.16.
 */
public class NavigationItem {

    private String name;
    private String UUID;
    private int newUpdates;
    private Date dateOfCreation;
    private Date lastUpdate;
    private Instance.Status status;

    public NavigationItem(String name, String uuid, int newUpdates, Date dateOfCreation, Date lastUpdate, Instance.Status status) {
        this.name = name;
        UUID = uuid;
        this.newUpdates = newUpdates;
        this.dateOfCreation = dateOfCreation;
        this.lastUpdate = lastUpdate;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getNewUpdates() {
        return newUpdates;
    }

    public void setNewUpdates(int newUpdates) {
        this.newUpdates = newUpdates;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Instance.Status getStatus() {
        return status;
    }

    public void setStatus(Instance.Status status) {
        this.status = status;
    }

    public String shortenedUUID() {
        return String.format("%s…", HashUtil.shorten(getUUID()));
    }
}
