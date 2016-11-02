package de.tu_bs.wire.simwatch.simulation;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.tu_bs.wire.simwatch.api.models.Attachment;
import de.tu_bs.wire.simwatch.net.HTTPAttachmentProvider;

/**
 * Provides Access to all Attachments. Must be informed of all Attachment changes via its public
 * methods or the Listener it implements. This class is a singleton
 */
public class AttachmentManager implements AttachmentDownloadListener {

    public static final String ATTACHMENT_DIR_NAME = "attachments";
    private static final String TAG = "AttachmentManager";
    private static final int[] retryTimes = {1000, 2000, 4000};
    private final Map<File, Attachment> downloadingFiles;
    // Stores which Update id the downloading version corresponds to
    private final Map<File, String> downloadingVersion;
    private final Multimap<Attachment, AttachmentDownloadListener> listeners;
    private final Map<File, Integer> file2tries;
    private Context context;
    private File attachmentDir;
    private AttachmentKnowledge attachmentKnowledge;

    protected AttachmentManager(Context context) {
        this.context = context;
        attachmentDir = new File(context.getFilesDir(), ATTACHMENT_DIR_NAME);
        attachmentKnowledge = new FileAttachmentKnowledge(context);
        boolean dirExists = attachmentDir.mkdirs();
        if (dirExists && !attachmentDir.isDirectory()) {
            Log.e(TAG, "Simulation directory already exists as a non-directory");
        }
        downloadingFiles = new HashMap<>();
        listeners = HashMultimap.create();
        downloadingVersion = new HashMap<>();
        file2tries = new HashMap<>();
    }

    /**
     * Registers an AttachmentDownloadListener on the Manager that listens for download results for
     * a single Attachment. Appropriate methods will be called on the listener whenever the Manager
     * receives new information about the Attachment's download
     *
     * @param attachment The Attachment to register a listener for
     * @param listener   The listener to be registered
     */
    public void addAttachmentListener(Attachment attachment, AttachmentDownloadListener listener) {
        synchronized (listeners) {
            listeners.put(attachment, listener);
        }
        File file = attachmentKnowledge.getFile(attachment);
        if (file != null) {
            if (file.exists() && file.canRead()) {
                listener.onDownloaded(file);
            }
        }
    }

    /**
     * Deletes and removes all information about all Attachments that are not contained in the given
     * Collection
     *
     * @param attachments Collection of all Attachments that should be kept
     */
    public void removeAllExcept(Collection<Attachment> attachments) {
        Collection<Attachment> knownAttachments = attachmentKnowledge.getAllAttachments();
        for (Attachment knownAttachment : knownAttachments) {
            if (!attachments.contains(knownAttachment)) {
                removeAttachment(knownAttachment);
            }
        }
    }

    /**
     * Deletes and removes all information about a single Attachment
     *
     * @param attachment Identifier of the Attachment to be deleted
     */
    private void removeAttachment(Attachment attachment) {
        File file = attachmentKnowledge.getFile(attachment);
        if (file != null && file.exists()) {
            if (file.canWrite() && file.delete()) {
                attachmentKnowledge.removeAttachment(attachment);
            } else {
                Log.e(TAG, "Cannot delete file for Attachment '" + attachment + "'");
            }
        } else {
            attachmentKnowledge.removeAttachment(attachment);
        }
    }

    /**
     * Unregisters all AttachmentDownloadListeners from the Manager
     */
    public void removeAllListeners() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

    /**
     * Downloads and from now on stored information about the given Attachment, if the Attachment is
     * not downloaded already or the downloaded version of the Attachment differs from the newest
     * version
     *
     * @param attachment Identifier of the Attachment to be downloaded
     * @param newestVersionID Version String of the newest version if the Attachment
     */
    public void download(Attachment attachment, String newestVersionID) {
        if (attachment == null) {
            throw new NullPointerException("attachment is null");
        }
        if (newestVersionID == null) {
            throw new NullPointerException("newestVersionID is null");
        }
        if (!attachmentKnowledge.has(attachment)) {
            File file = fileFromAttachmentName(attachment);
            attachmentKnowledge.addAttachment(attachment, "", file);
        }
        String version = attachmentKnowledge.getVersion(attachment);
        File file = attachmentKnowledge.getFile(attachment);
        if (!version.equals(newestVersionID)) {
            onFileChanged(file, newestVersionID);
        }
    }

    /**
     * Get the file that the given Attachment should be stored in
     *
     * @param attachment Identifier of the Attachment in question
     * @return The Attachment's file location
     */
    public File fileFromAttachmentName(Attachment attachment) {
        return fileFromAttachmentName(attachment, 0);
    }

    /**
     * Get the file that the given Attachment should be stored in. If i is not 0, get the i'th
     * alternative file for that attachment
     *
     * @param attachment The attachment that requires a file
     * @param i          The index of the alternative that is desired
     * @return The i'th alternative file pointer for the attachment
     */
    private File fileFromAttachmentName(Attachment attachment, int i) {
        return new File(attachmentDir, getFileName(attachment, i));
    }

    protected String getFileName(Attachment attachment, int i) {
        return attachment.getInstanceID() + "_" + i + "_" + attachment.getAttachmentName();
    }

    private void onFileChanged(File file, String newVersion) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        if (newVersion == null) {
            throw new NullPointerException("newVersion is null");
        }
        Attachment attachment = attachmentKnowledge.getAttachment(file);
        File altFile = fileFromAttachmentName(attachment, 1);
        synchronized (downloadingFiles) {
            if (!downloadingFiles.containsKey(altFile)) {
                downloadingFiles.put(altFile, attachment);
                downloadingVersion.put(altFile, newVersion);
                download(attachment, altFile);
            }
        }
    }

    /**
     * Downloads the given Attachment to the given file location
     *
     * @param attachment Identifier of the Attachment to be downloaded
     * @param file The desired file location
     */
    private void download(Attachment attachment, File file) {
        new HTTPAttachmentProvider(this, context).download(attachment, file);
    }

    @Override
    public void onDownloaded(File file) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        Attachment attachment;
        String newVersion;
        synchronized (downloadingFiles) {
            attachment = downloadingFiles.remove(file);
            newVersion = downloadingVersion.remove(file);
        }
        if (attachment == null) {
            failDownloading(file);
            Log.e(TAG, "Attachment of downloaded file was null");
            return;
        }
        synchronized (file2tries) {
            file2tries.remove(file);
        }
        File original = attachmentKnowledge.getFile(attachment);
        if (original == null) {
            failDownloading(file);
            Log.e(TAG, "original file of downloaded file was null");
            return;
        }
        Log.d(TAG, "attachment: " + attachment + ", newVersion: " + newVersion + ", file: " + file + ", original: " + original);
        boolean fileMoved = false;
        for (int tries = 3; tries > 0 && !fileMoved; tries--) {
            try {
                Files.move(file, original);
                fileMoved = true;
            } catch (IOException e) {
                Log.e(TAG, "Couldn't move file", e);
            }
        }
        if (fileMoved) {
            attachmentKnowledge.addAttachment(attachment, newVersion, original);
        }
        synchronized (listeners) {
            for (AttachmentDownloadListener listener : listeners.get(attachment)) {
                listener.onDownloaded(original);
            }
        }
    }

    @Override
    public void onCouldNotDownload(File file) {
        retry(file, false);
    }

    @Override
    public void onRetryLater(File file) {
        retry(file, true);
    }

    /**
     * Retries downloading the Attachment corresponding to the given file a couple of times or
     * indefinitely, if force is true
     *
     * @param file The file corresponding to the Attachment
     * @param force Whether the download should be retried indefinitely
     */
    private void retry(final File file, boolean force) {
        Integer tries = file2tries.get(file);
        if (tries == null) {
            tries = 0;
        }
        if (tries < retryTimes.length || force) {
            file2tries.put(file, tries + 1);
            final Integer triesSoFar = Math.min(tries, retryTimes.length - 1);
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(retryTimes[triesSoFar]);
                        Attachment attachment = attachmentKnowledge.getAttachment(file);
                        if (attachment == null) {
                            failDownloading(file);
                        } else {
                            download(attachment, file);
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Interrupted while waiting to retry downloading", e);
                        failDownloading(file);
                    }
                }
            }.start();
        } else {
            failDownloading(file);
        }
    }

    /**
     * Stops retrying to download the Attachment corresponding to the given file
     *
     * @param file The file corresponding to the Attachment
     */
    private void failDownloading(File file) {
        Attachment attachment = attachmentKnowledge.getAttachment(file);
        File original = attachmentKnowledge.getFile(attachment);
        listeners.removeAll(attachment);
        synchronized (downloadingFiles) {
            downloadingFiles.remove(file);
            downloadingVersion.remove(file);
        }
        synchronized (listeners) {
            for (AttachmentDownloadListener listener : listeners.get(attachment)) {
                listener.onCouldNotDownload(original);
            }
        }
    }
}
