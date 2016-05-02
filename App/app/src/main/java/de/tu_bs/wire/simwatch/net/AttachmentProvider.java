package de.tu_bs.wire.simwatch.net;

import java.io.File;
import java.net.URL;

import de.tu_bs.wire.simwatch.api.models.Attachment;
import de.tu_bs.wire.simwatch.simulation.AttachmentDownloadListener;

/**
 * Provides access to Attachments for one specific Instance
 */
public abstract class AttachmentProvider {

    protected AttachmentDownloadListener listener;

    public AttachmentProvider(AttachmentDownloadListener listener) {
        this.listener = listener;
    }

    public abstract void download(Attachment attachment, File outputFile);

    public abstract void download(URL url, File outputFile);
}
