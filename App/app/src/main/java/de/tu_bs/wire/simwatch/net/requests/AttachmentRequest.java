package de.tu_bs.wire.simwatch.net.requests;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.api.models.Attachment;
import de.tu_bs.wire.simwatch.net.UpdateSettings;

/**
 * Created by mw on 19.01.16.
 */
public class AttachmentRequest {

    public static final String ATTACHMENT_URL = "%s/instance/%s/attachment/%s";
    private static final String TAG = "AttachmentRequest";
    private String instanceID;
    private String attachmentName;
    private Context context;

    public AttachmentRequest(Context context, Attachment attachment) {
        this.context = context;
        this.instanceID = attachment.getInstanceID();
        this.attachmentName = attachment.getAttachmentName();
    }

    public URL getURL() {
        try {
            String serverAddress = new UpdateSettings(context).getServerAddress();
            return new URL(String.format(ATTACHMENT_URL, serverAddress, instanceID, attachmentName));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
