package de.tu_bs.wire.simwatch.net.requests;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.net.UpdateSettings;

/**
 * Class for building a URL for a single request retrieving all new updates for a simulation
 * instance
 */
public class UpdateRequest {

    public static final String UPDATES_URL = "%s/instance/%s/updates/-1";
    public static final String NEW_UPDATES_URL = "%s/instance/%s/updates/%s";
    private static final String TAG = "UpdateRequest";

    private Instance instance;
    private Context context;

    public UpdateRequest(Context context, Instance instance) {
        this.context = context;
        this.instance = instance;
    }

    public URL getURL() {
        String id = instance.getID();
        try {
            String serverAddress = new UpdateSettings(context).getServerAddress();
            if (instance.getNumberOfUpdates() > 0) {
                Update lastUpdate = instance.getLastUpdate();
                String lastUpdateID = lastUpdate.getID();
                return new URL(String.format(NEW_UPDATES_URL, serverAddress, id, lastUpdateID));
            } else {
                return new URL(String.format(UPDATES_URL, serverAddress, id));
            }
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }

}
