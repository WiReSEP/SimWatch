package de.tu_bs.wire.simwatch.net.requests;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Update;

/**
 * Created by mw on 19.01.16.
 */
public class UpdateRequest {

    //dates are in ISO8601

    public static final String UPDATES_URL = "http://aquahaze.de:5001/instance/%s/updates/-1";
    public static final String NEW_UPDATES_URL = "http://aquahaze.de:5001/instance/%s/updates/%s";
    private static final String TAG = "UpdateRequest";

    private Instance instance;

    public UpdateRequest(Instance instance) {
        this.instance = instance;
    }

    public URL getURL() {
        String id = instance.getID();
        List<Update> updates = instance.getUpdates();
        try {
            if (!updates.isEmpty()) {
                Update lastUpdate = updates.get(updates.size() - 1);
                String lastUpdateID = lastUpdate.getID();
                return new URL(String.format(NEW_UPDATES_URL, id, lastUpdateID));
            } else {
                return new URL(String.format(UPDATES_URL, id));
            }
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }

}
