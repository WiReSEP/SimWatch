package de.tu_bs.wire.simwatch.net.requests;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.net.UpdateSettings;

/**
 * Created by mw on 19.01.16.
 */
public class InstanceRequest {

    public static final String INSTANCE_URL = "%s/instance/%s";
    private static final String TAG = "InstanceRequest";

    private String id;
    private Context context;

    public InstanceRequest(Context context, String id) {
        this.context = context;
        this.id = id;
    }

    public URL getURL() {
        try {
            String serverAddress = new UpdateSettings(context).getServerAddress();
            return new URL(String.format(INSTANCE_URL, serverAddress, id));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
