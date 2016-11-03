package de.tu_bs.wire.simwatch.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.api.types.Matrix;
import de.tu_bs.wire.simwatch.api.types.Vector;
import okio.BufferedSink;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class to compose and send updates. Use the <code>put</code> and <code>attach</code>
 * methods to add properties.Call {@link #post()} when done. For convenience most methods are
 * chainable.
 * <p>
 * Example:
 * <pre><code>
 * client.buildUpdate()
 *   .put("finished", false)
 *   .put("iteration", 4654)
 *   .put("progress", 0.4)
 *   .attach("render", new AttachmentStreamer() {
 *     {@literal @}Override
 *      public void writeTo(OutputStream outputStream) throws IOException {
 *          ImageIO.write(image, "png", outputStream);
 *      }
 *   })
 *   .post();
 * </code></pre>
 */
@SuppressWarnings("WeakerAccess") /* api methods must be public  */
public final class UpdateBuilder {
    private static final Gson gson = new Gson();
    private static final MediaType BINARY_MEDIA_TYPE = MediaType.parse("application/octet-stream");
    private SimWatchClient connector;
    private JsonObject data;
    private Map<String, RequestBody> attachments;

    /*package*/ UpdateBuilder(SimWatchClient connector) {
        this.connector = connector;
        this.data = new JsonObject();
        this.attachments = new HashMap<>();
    }

    public UpdateBuilder put(String propertyName, Number value) {
        data.addProperty(propertyName, value);
        return this;
    }

    public UpdateBuilder put(String propertyName, String value) {
        data.addProperty(propertyName, value);
        return this;
    }

    public UpdateBuilder put(String propertyName, Boolean value) {
        data.addProperty(propertyName, value);
        return this;
    }

    public UpdateBuilder put(String propertyName, Matrix matrix) {
        data.add(propertyName, gson.toJsonTree(matrix));
        return this;
    }

    public UpdateBuilder put(String propertyName, Vector vector) {
        data.add(propertyName, gson.toJsonTree(vector));
        return this;
    }

    public UpdateBuilder attach(String propertyName, byte[] data) {
        attachments.put(propertyName, RequestBody.create(BINARY_MEDIA_TYPE, data));
        return this;
    }

    /**
     * Attach a file as binary property to the update object.
     *
     * @param propertyName Name of the property as defined in the simulation profile
     * @param file         File to read the data from
     * @return this UpdateBuilder for method chaining
     */
    public UpdateBuilder attach(String propertyName, File file) {
        attachments.put(propertyName, RequestBody.create(BINARY_MEDIA_TYPE, file));
        return this;
    }

    /**
     * Attach a binary property to the update object.
     * <p><b>Warning</b>: the entire content of the stream will be buffered in memory.
     * If this is a problem, use {@link #attach(String, File)} instead</p>
     *
     * @param propertyName Name of the property as defined in the simulation profile
     * @param streamer     A {@link AttachmentStreamer}. Will be called when posting the update
     * @return this UpdateBuilder for method chaining
     */
    public UpdateBuilder attach(String propertyName, AttachmentStreamer streamer) {
        attachments.put(propertyName, new BufferedRequestBody(streamer));
        return this;
    }


    /**
     * Alias for {@link #attach(String, File) attach(propertyName, new File(fileName))}
     *
     * @param propertyName Name of the property as defined in the simulation profile
     * @param fileName     Name of the file to send in this update.
     * @return this UpdateBuilder for method chaining
     */
    public UpdateBuilder attachFile(String propertyName, String fileName) {
        return attach(propertyName, new File(fileName));
    }

    /**
     * Construct the simulation update and send it to the backend. This is a synchronous call
     * and will block until finished. Errors are logged.
     */
    public void post() {
        connector.update(new Update(data, attachments.keySet()), attachments);
    }

    /**
     * Wrapper class to convert {@link AttachmentStreamer} into an HTTP request body.
     * Buffers the entire data from the StreamWriter to determine the content-length
     * header. This is necessary if the backend does not support chunked transfers.
     */
    private static class BufferedRequestBody extends RequestBody {
        private final AttachmentStreamer streamer;
        private byte[] buffer;

        public BufferedRequestBody(AttachmentStreamer streamer) {
            this.streamer = streamer;
        }

        private void fillBuffer() throws IOException {
            if (buffer == null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                streamer.writeTo(outputStream);
                buffer = outputStream.toByteArray();
            }
        }

        @Override
        public long contentLength() throws IOException {
            fillBuffer();
            return buffer.length;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            fillBuffer();
            streamer.writeTo(sink.outputStream());
        }

        @Override
        public MediaType contentType() {
            return BINARY_MEDIA_TYPE;
        }
    }
}
