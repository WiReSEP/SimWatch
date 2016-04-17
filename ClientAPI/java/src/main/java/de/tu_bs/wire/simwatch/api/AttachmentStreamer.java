package de.tu_bs.wire.simwatch.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A class that implements this interface supplies a binary update
 * property by writing it to an {@link OutputStream}
 */
@SuppressWarnings("WeakerAccess") // public api
public interface AttachmentStreamer {
    /**
     * Write data to a supplied OutputStream. May be called multiple times with different
     * streams.
     *
     * @param outputStream OutputStream to write to
     * @throws IOException If the writing fails
     */
    void writeTo(OutputStream outputStream) throws IOException;
}
