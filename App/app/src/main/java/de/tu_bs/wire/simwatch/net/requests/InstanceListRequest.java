package de.tu_bs.wire.simwatch.net.requests;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.net.UpdateSettings;

/**
 * Created by mw on 19.01.16.
 */
public class InstanceListRequest {

    public static final String INSTANCE_LIST_URL = "%s/instance/ids";
    private static final String TAG = "InstanceListRequest";
    private Context context;

    public InstanceListRequest(Context context) {
        this.context = context;
    }

    public URL getURL() {
        try {
            String serverAddress = new UpdateSettings(context).getServerAddress();
            return new URL(String.format(INSTANCE_LIST_URL, serverAddress));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
