package de.tu_bs.wire.simwatch.api.models;


import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Update {

    public Set<String> attachments;
    @SerializedName("update_id")
    private String ID;
    private JsonObject data;
    @SerializedName("date")
    private Date dateOfCreation;

    public Update(JsonObject data, Set<String> attachments) {
        this(null, data, attachments);

    }

    public Update(String ID, JsonObject data, Set<String> attachments) {
        this.ID = ID;
        this.data = data;
        this.attachments = new HashSet<>(attachments);
    }

    public String getID() {
        return ID;
    }

    public JsonObject getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Update)) return false;

        Update update = (Update) o;

        return ID != null ? ID.equals(update.ID) : update.ID == null;

    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }
}
