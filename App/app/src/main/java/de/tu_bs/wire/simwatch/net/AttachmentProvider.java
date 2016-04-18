package de.tu_bs.wire.simwatch.net;

import java.io.File;
import java.net.URL;
import java.util.Date;

import de.tu_bs.wire.simwatch.simulation.AttachmentDownloadListener;

/**
 * Provides access to Attachments for one specific Instance
 */
public abstract class AttachmentProvider {

    protected AttachmentDownloadListener listener;

    public AttachmentProvider(AttachmentDownloadListener listener) {
        this.listener = listener;
    }

    public abstract void checkForChange(String attachmentName, Date lastModified, File outputFile);

    public abstract void checkForChange(URL url, Date lastModified, File outputFile);

    public abstract void download(String attachmentName, File outputFile);

    public abstract void download(URL url, File outputFile);
}
