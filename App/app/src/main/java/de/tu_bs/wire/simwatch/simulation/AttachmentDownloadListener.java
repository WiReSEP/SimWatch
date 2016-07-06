package de.tu_bs.wire.simwatch.simulation;

import java.io.File;

/**
 * Listener to be called by an AttachmentProvider when the retrieval of an Attachment ended
 * successfully or unsuccessfully
 */
public interface AttachmentDownloadListener {

    /**
     * Called when an Attachment was successfully downloaded and saved in the given file
     *
     * @param file The file, where the Attachment was saved
     */
    void onDownloaded(File file);

    /**
     * Called when the download of an Attachment failed for unforeseen reasons. Note that if the
     * download failed because the server is currently replacing it with a newer version,
     * onRetryLater is called instead
     *
     * @param file The file where the Attachment should have been saved, had its download been
     *             successful
     */
    void onCouldNotDownload(File file);

    /**
     * Called when the attachment server was successfully reached, but suggested to retry the
     * download later instead. This is typically the case when the Attachment is currently being
     * replaced with a newer version
     *
     * @param file The file where the Attachment should have been saved, had its download been
     *             successful
     */
    void onRetryLater(File file);
}
