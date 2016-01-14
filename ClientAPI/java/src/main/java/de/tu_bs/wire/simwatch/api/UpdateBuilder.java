package de.tu_bs.wire.simwatch.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.api.types.Matrix;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class UpdateBuilder {
    private final Gson gson;
    private ApiConnector connector;
    private JsonObject data;
    private Map<String, InputStream> attachments;

    /*package*/ UpdateBuilder(ApiConnector connector) {
        this.connector = connector;
        this.data = new JsonObject();
        this.attachments = new HashMap<>();
        gson = new Gson();
    }

    public UpdateBuilder put(String propertyName, Number value) {
        data.addProperty(propertyName, value);
        return this;
    }

    public UpdateBuilder put(String property, String value) {
        data.addProperty(property, value);
        return this;
    }

    public UpdateBuilder put(String property, Boolean value) {
        data.addProperty(property, value);
        return this;
    }

    public UpdateBuilder put(String property, Matrix matrix) {
        data.add(property, gson.toJsonTree(matrix));
        return this;
    }

    public UpdateBuilder put(String property, Number[] values) {
        data.add(property, toJsonArray(values));
        return this;
    }

    public UpdateBuilder put(String property, InputStream stream) {
        attachments.put(property, stream);
        return this;
    }

    public UpdateBuilder put(String property, byte[] data) {
        attachments.put(property, new ByteArrayInputStream(data));
        return this;
    }

    @NotNull
    private JsonArray toJsonArray(Object[] values) {
        JsonArray jsonArray = new JsonArray();
        for (Object value : values) {
            jsonArray.add(gson.toJsonTree(value));
        }
        return jsonArray;
    }

    public void post() {
        connector.update(new Update(data, attachments.keySet()));
    }
}
