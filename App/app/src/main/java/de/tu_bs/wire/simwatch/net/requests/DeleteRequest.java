package de.tu_bs.wire.simwatch.net.requests;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.net.UpdateSettings;

/**
 * Class for building a URL for a single request deleting an instance as well as all its updates and
 * attachments
 */
public class DeleteRequest {

    public static final String DELETE_URL = "%s/instance/delete";
    public static final MediaType POST_MEDIA_TYPE = MediaType.parse("application/json");
    private static final String DELETE_PROPERTY_NAME = "_id";
    private static final String TAG = "DeleteRequest";
    private String instanceID;
    private Context context;


    public DeleteRequest(Context context, String instanceID) {
        this.context = context;
        this.instanceID = instanceID;
    }

    public URL getURL() {
        try {
            String serverAddress = new UpdateSettings(context).getServerAddress();
            return new URL(String.format(DELETE_URL, serverAddress));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }

    public MediaType getPostMediaType() {
        return POST_MEDIA_TYPE;
    }

    public String getPOSTData() {
        JsonObject postObject = new JsonObject();
        postObject.addProperty(DELETE_PROPERTY_NAME, instanceID);
        return postObject.toString();
    }
}
