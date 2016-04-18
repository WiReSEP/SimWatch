package de.tu_bs.wire.simwatch.net.requests;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mw on 19.01.16.
 */
public class InstanceRequest {

    public static final String INSTANCE_URL = "http://aquahaze.de:5001/instance/%s";
    private static final String TAG = "InstanceRequest";

    private String id;

    public InstanceRequest(String id) {
        this.id = id;
    }

    public URL getURL() {
        try {
            return new URL(String.format(INSTANCE_URL, id));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
