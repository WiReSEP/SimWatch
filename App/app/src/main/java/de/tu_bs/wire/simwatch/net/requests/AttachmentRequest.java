package de.tu_bs.wire.simwatch.net.requests;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.api.models.Attachment;

/**
 * Created by mw on 19.01.16.
 */
public class AttachmentRequest {

    public static final String ATTACHMENT_URL = "http://aquahaze.de:5001/instance/%s/attachment/%s";
    private static final String TAG = "AttachmentRequest";
    private String instanceID;
    private String attachmentName;

    public AttachmentRequest(Attachment attachment) {
        this.instanceID = attachment.getInstanceID();
        this.attachmentName = attachment.getAttachmentName();
    }

    public URL getURL() {
        try {
            return new URL(String.format(ATTACHMENT_URL, instanceID, attachmentName));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
