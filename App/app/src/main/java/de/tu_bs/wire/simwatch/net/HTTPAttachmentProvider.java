package de.tu_bs.wire.simwatch.net;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import de.tu_bs.wire.simwatch.api.models.Attachment;
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
    public void download(Attachment attachment, File outputFile) {
        AttachmentRequest attachmentRequest = new AttachmentRequest(context, attachment);
        URL url = attachmentRequest.getURL();

        if (url != null) {
            download(url, outputFile);
        }
    }

    @Override
    public void download(final URL url, final File outputFile) {
        if (url != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Downloading from '" + url + "'");

                        URLConnection urlConnection = url.openConnection();
                        if (!(urlConnection instanceof HttpURLConnection)) {
                            Log.d(TAG, "URL '" + url + "' does not open a httpConnection");
                            throw new IOException(String.format("Cannot open http connection: URL: '%s'", url));
                        }
                        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                        InputStream is = httpUrlConnection.getInputStream();
                        DataInputStream dis = new DataInputStream(is);

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int length;

                        FileOutputStream os = new FileOutputStream(outputFile);
                        while ((length = dis.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                        os.flush();
                        os.close();
                        listener.onDownloaded(outputFile);
                    } catch (IOException e) {
                        Log.e(TAG, "IO error", e);
                        listener.onCouldNotDownload(outputFile);
                    }
                }
            }.start();
        }
    }
}
