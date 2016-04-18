package de.tu_bs.wire.simwatch.net.requests;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mw on 19.01.16.
 */
public class AttachmentRequest {

    public static final String ATTACHMENT_URL = "http://aquahaze.de/%s";
    private static final String TAG = "AttachmentRequest";
    private String attachmentName;

    public AttachmentRequest(String attachmentName) {

        this.attachmentName = attachmentName;
    }

    public URL getURL() {
        try {
            return new URL(String.format(ATTACHMENT_URL,attachmentName));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
