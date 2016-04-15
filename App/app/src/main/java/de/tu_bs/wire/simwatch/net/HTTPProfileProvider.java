package de.tu_bs.wire.simwatch.net;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.net.requests.ProfileRequest;
import de.tu_bs.wire.simwatch.simulation.profile.ProfileAcquisitionListener;

/**
 * ProfileProvider using HTTP. This should be standard
 */
public class HTTPProfileProvider extends ProfileProvider {

    private static final String TAG = "ProfileProvider";
    private final OkHttpClient client;
    private Context context;

    public HTTPProfileProvider(ProfileAcquisitionListener listener, Context context) {
        super(listener);
        this.context = context;
        client = new OkHttpClient();
    }

    @Override
    public void acquireProfile(final String id) {
        new Thread() {
            @Override
            public void run() {
                ProfileRequest profileRequest = new ProfileRequest(id);
                Request request = new Request.Builder().url(profileRequest.getURL()).build();
                Response response;
                try {
                    response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseString = response.body().string();
                        //responseString = "{\"_id\":\"proID\",\"name\":\"mein Profil\"," +
                        //        "\"properties\":{\"var\":\"number\",\"id\":\"plotReference\",\"line\":\"plottable\"}}"; //fixme DEBUG
                        Profile profile = null;
                        Log.d(TAG, "Received profile '" + id + "': " + responseString);
                        try {
                            profile = new Gson().fromJson(responseString, Profile.class);
                        } catch (JsonSyntaxException e) {
                            Log.e(TAG, "Received Profile '" + id + "' with broken syntax", e);
                        }
                        listener.onProfileAcquired(profile);
                    } else {
                        Log.e(TAG, "Cannot retrieve Profile " + id + ". Failed with HTTP status code " + response.code());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Cannot retrieve Profile " + id, e);
                }
            }
        }.start();
    }

}
