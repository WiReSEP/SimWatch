package de.tu_bs.wire.simwatch.net.requests;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.net.UpdateSettings;

/**
 * Class for building a URL for a single request retrieving all new updates for a simulation
 * instance
 */
public class StatusUpdateRequest {

    public static final String UPDATES_URL = "%s/instance/%s/status";
    private static final String TAG = "StatusUpdateRequest";

    private Instance instance;
    private Context context;

    public StatusUpdateRequest(Context context, Instance instance) {
        this.context = context;
        this.instance = instance;
    }

    public URL getURL() {
        String id = instance.getID();
        try {
            String serverAddress = new UpdateSettings(context).getServerAddress();
            return new URL(String.format(UPDATES_URL, serverAddress, id));
        } catch (MalformedURLException e) {
            Log.e(TAG, "Created malformed URL", e);
            return null;
        }
    }

}
