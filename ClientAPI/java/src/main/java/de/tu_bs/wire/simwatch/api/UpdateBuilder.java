package de.tu_bs.wire.simwatch.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.api.types.Matrix;
import de.tu_bs.wire.simwatch.api.types.Vector;
import okio.BufferedSink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public final class UpdateBuilder {
    private static final Gson gson = new Gson();
    private static final MediaType BINARY_MEDIA_TYPE = MediaType.parse("application/octet-stream");
    private ApiConnector connector;
    private JsonObject data;
    private Map<String, RequestBody> attachments;

    /*package*/ UpdateBuilder(ApiConnector connector) {
        this.connector = connector;
        this.data = new JsonObject();
        this.attachments = new HashMap<>();
    }

    public UpdateBuilder put(String propertyName, Number value) {
        data.addProperty(propertyName, value);
        return this;
    }

    public UpdateBuilder put(String propertyName, int[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder put(String propertyName, long[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder put(String propertyName, float[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder put(String propertyName, double[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder put(String propertyName, String value) {
        data.addProperty(propertyName, value);
        return this;
    }

    public UpdateBuilder put(String propertyName, String[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder put(String propertyName, Boolean value) {
        data.addProperty(propertyName, value);
        return this;
    }

    public UpdateBuilder put(String propertyName, boolean[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder put(String propertyName, Matrix matrix) {
        data.add(propertyName, gson.toJsonTree(matrix));
        return this;
    }

    public UpdateBuilder put(String propertyName, Matrix[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder put(String propertyName, Vector vector) {
        data.add(propertyName, gson.toJsonTree(vector));
        return this;
    }

    public UpdateBuilder put(String propertyName, Vector[] values) {
        data.add(propertyName, gson.toJsonTree(values));
        return this;
    }

    public UpdateBuilder attach(String propertyName, byte[] data) {
        attachments.put(propertyName, RequestBody.create(BINARY_MEDIA_TYPE, data));
        return this;
    }

    public UpdateBuilder attach(String propertyName, StreamWriter streamWriter) {
        attachments.put(propertyName, new WriterRequestBody(streamWriter));
        return this;
    }

    public void post() {
        connector.update(new Update(data, attachments.keySet()));
    }

    public interface StreamWriter {
        void writeTo(OutputStream outputStream) throws IOException;
    }

    private static class WriterRequestBody extends RequestBody {
        private final StreamWriter streamWriter;

        public WriterRequestBody(StreamWriter streamWriter) {
            this.streamWriter = streamWriter;
        }

        @Override
        public MediaType contentType() {
            return BINARY_MEDIA_TYPE;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            streamWriter.writeTo(sink.outputStream());
        }
    }
}
