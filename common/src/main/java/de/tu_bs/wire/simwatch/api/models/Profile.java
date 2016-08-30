package de.tu_bs.wire.simwatch.api.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.GsonUtil;
import de.tu_bs.wire.simwatch.api.types.Types;

/**
 * Simple class to store information about the profile a simulation instance has
 */
public class Profile {

    @SerializedName("_id")
    private String ID;
    private String name;
    private JsonObject properties;
    private Map<String, String> propertyMap;

    public Profile(String name, JsonObject properties) {
        this.name = name;
        this.properties = properties;
        propertyMap = null;
    }

    public static Profile fromString(String str) {
        try {
            return GsonUtil.getGson().fromJson(str, Profile.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;

    }

    public Map<String, String> getProperties() {
        if (propertyMap == null) {
            calculatePropertyMap();
        }
        return propertyMap;
    }

    private void calculatePropertyMap() {
        propertyMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            JsonElement type = entry.getValue();
            if (type.isJsonPrimitive()) {
                String typeAsString = type.getAsString();
                if (Types.isValidType(typeAsString)) {
                    propertyMap.put(entry.getKey(), typeAsString);
                }
            }
        }
    }

    public boolean hasPlotReference() {
        for (Map.Entry<String, String> property : getProperties().entrySet()) {
            if (Types.getType(property.getValue()) == Types.Type.PLOT_REFERENCE) {
                return true;
            }
        }
        return false;
    }

    public String getPlotReferenceKey() {
        if (!hasPlotReference()) {
            throw new IllegalStateException("This Profile doesn't have a plot reference");
        }
        String referenceKey = null;
        for (Map.Entry<String, String> property : getProperties().entrySet()) {
            if (Types.getType(property.getValue()) == Types.Type.PLOT_REFERENCE) {
                referenceKey = property.getKey();
                break;
            }
        }
        return referenceKey;
    }
}
