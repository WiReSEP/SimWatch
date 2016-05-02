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
 * Created by mw on 18.04.16.
 */
public class AttachmentManager implements AttachmentDownloadListener {

    public static final String ATTACHMENT_DIR_NAME = "attachments";
    private static final String TAG = "AttachmentManager";
    private static final int[] retryTimes = {1000, 2000, 4000};
    protected static AttachmentManager instance;
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

    public static AttachmentManager getInstance() {
        if (instance==null) {
            throw new IllegalStateException("AttachmentManager not initialized");
        }
        return instance;
    }

    public static AttachmentManager getInstance(Context context) {
        if (instance == null) {
            instance = new AttachmentManager(context);
        }
        return instance;
    }

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

    public void removeAllExcept(Collection<Attachment> attachments) {
        Collection<Attachment> knownAttachments = attachmentKnowledge.getAllAttachments();
        for (Attachment knownAttachment : knownAttachments) {
            if (!attachments.contains(knownAttachment)) {
                removeAttachment(knownAttachment);
            }
        }
    }

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

    public void removeAllListeners() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

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

    public void onFileChanged(File file, String newVersion) {
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

    private void retry(final File file, boolean force) {
        Integer tries = file2tries.get(file);
        if (tries == null) {
            tries = 0;
        }
        if (tries < retryTimes.length || force) {
            file2tries.put(file, tries + 1);
            final Integer finalTries = Math.min(tries, retryTimes.length - 1);
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(retryTimes[finalTries]);
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
