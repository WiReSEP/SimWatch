package de.tu_bs.wire.simwatch.net.requests;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mw on 09.02.16.
 */
public class ProfileRequest {

    public static final String PROFILE_URL = "http://aquahaze.de:5001/profile/%s";
    private static final String TAG = "ProfileRequest";

    private String id;

    public ProfileRequest(String id) {
        this.id = id;
    }

    public URL getURL() {
        try {
            return new URL(String.format(PROFILE_URL, id));
        } catch (MalformedURLException e) {
            Log.e(TAG,"Created malformed URL",e);
            return null;
        }
    }
}
