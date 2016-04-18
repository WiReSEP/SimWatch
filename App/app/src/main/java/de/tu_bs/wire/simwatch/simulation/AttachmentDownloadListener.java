package de.tu_bs.wire.simwatch.simulation;

import java.io.File;

/**
 * Created by mw on 17.04.16.
 */
public interface AttachmentDownloadListener {

    void onFileChanged(File file);

    void onFileUnchanged(File file);

    void onDownloaded(File file);

    void onCouldNotCheckForChange(File file);

    void onCouldNotDownload(File file);
}
