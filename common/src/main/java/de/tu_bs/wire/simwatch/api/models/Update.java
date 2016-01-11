package de.tu_bs.wire.simwatch.api.models;


import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class Update {

    public Set<String> attachments;
    private JsonObject data;

    public Update(JsonObject data, Set<String> attachments) {
        this.data = data;
        this.attachments = new HashSet<>(attachments);
    }

    public JsonObject getData() {
        return data;
    }
}
