package de.tu_bs.wire.simwatch.net.requests;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.tu_bs.wire.simwatch.net.UpdateSettings;

/**
 * Class for building a URL for a single request retrieving a simulation profile
 */
public class ProfileRequest {

    public static final String PROFILE_URL = "%s/profile/%s";
    private static final String TAG = "ProfileRequest";

    private String id;
    private Context context;

    public ProfileRequest(Context context, String id) {
        this.context = context;
        this.id = id;
    }

    public URL getURL() {
        try {
            String serverAddress = new UpdateSettings(context).getServerAddress();
            return new URL(String.format(PROFILE_URL, serverAddress, id));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
