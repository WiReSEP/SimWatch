package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Simple class to store information about the profile a simulation instance has
 */
public class Profile {

    @SerializedName("_id")
    private String ID;
    private String name;
    private JsonObject properties;

    public Profile(String name, JsonObject properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public JsonObject getProperties() {
        return properties;
    }
}
