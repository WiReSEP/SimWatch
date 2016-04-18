package de.tu_bs.wire.simwatch.net;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import de.tu_bs.wire.simwatch.net.requests.AttachmentRequest;
import de.tu_bs.wire.simwatch.simulation.AttachmentDownloadListener;

/**
 * AttachmentProvider using HTTP. This should be standard
 */
public class HTTPAttachmentProvider extends AttachmentProvider {
    private static final String TAG = "HTTPAttachmentProvider";
    private static final int BUFFER_SIZE = 1024;
    private Context context;

    public HTTPAttachmentProvider(AttachmentDownloadListener listener, Context context) {
        super(listener);
        this.context = context;
    }

    @Override
    public void checkForChange(String attachmentName, Date lastModified, File outputFile) {
        AttachmentRequest attachmentRequest = new AttachmentRequest(attachmentName);
        URL url = attachmentRequest.getURL();

        if (url != null) {
            checkForChange(url, lastModified, outputFile);
        } else {
            listener.onCouldNotCheckForChange(outputFile);
        }
    }

    @Override
    public void checkForChange(URL url, Date lastModified, File outputFile) {
        try {
            URLConnection connection= url.openConnection();
            Date newLastModified = new Date(connection.getLastModified());
            if (lastModified.equals(newLastModified)) {
                listener.onFileUnchanged(outputFile);
            } else {
                listener.onFileChanged(outputFile);
            }
        } catch (IOException e) {
            Log.e(TAG,"IOException checking for change",e);
            listener.onCouldNotCheckForChange(outputFile);
        }
    }

    @Override
    public void download(String attachmentName, File outputFile) {
        AttachmentRequest attachmentRequest = new AttachmentRequest(attachmentName);
        URL url = attachmentRequest.getURL();

        if (url != null) {
            download(url, outputFile);
        }
    }

    @Override
    public void download(URL url, File outputFile) {
        try {
            InputStream is = url.openStream();
            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[BUFFER_SIZE];
            int length;

            FileOutputStream os = new FileOutputStream(outputFile);
            while ((length = dis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            listener.onDownloaded(outputFile);
        } catch (IOException e) {
            Log.e(TAG, "IO error", e);
            listener.onCouldNotDownload(outputFile);
        } catch (SecurityException e) {
            Log.e(TAG, "Security error", e);
            listener.onCouldNotDownload(outputFile);
        }
    }
}
