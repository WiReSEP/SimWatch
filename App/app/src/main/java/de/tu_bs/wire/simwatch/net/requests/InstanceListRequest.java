package de.tu_bs.wire.simwatch.net.requests;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mw on 19.01.16.
 */
public class InstanceListRequest {

    public static final String INSTANCE_LIST_URL = "http://aquahaze.de:5001/instance/ids";
    private static final String TAG = "InstanceListRequest";

    public InstanceListRequest() {

    }

    public URL getURL() {
        try {
            return new URL(INSTANCE_LIST_URL);
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
