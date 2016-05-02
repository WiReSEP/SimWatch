package de.tu_bs.wire.simwatch.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import de.tu_bs.wire.simwatch.ui.activities.SettingsActivity;

/**
 * Created by mw on 25.04.16.
 */
public class UpdateSettings {


    private Context context;

    public UpdateSettings(Context context) {
        this.context = context;
    }

    public boolean autoUpdateEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String autoUpdateWhen = sharedPref.getString(SettingsActivity.AUTO_UPDATE_WHEN, "");
        switch (autoUpdateWhen) {
            case "always":
                return true;
            case "wifi":
                ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                return mWifi.isConnected();
            case "manually":
            default:
                return false;
        }
    }

}
