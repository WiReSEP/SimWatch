package de.tu_bs.wire.simwatch.net;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.tu_bs.wire.simwatch.api.GsonUtil;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.net.requests.DeleteRequest;
import de.tu_bs.wire.simwatch.net.requests.InstanceListRequest;
import de.tu_bs.wire.simwatch.net.requests.InstanceRequest;
import de.tu_bs.wire.simwatch.simulation.InstanceAcquisitionListener;

/**
 * InstanceProvider using HTTP. This should be standard
 */
public class HTTPInstanceProvider extends InstanceProvider {

    private static final String TAG = "HTTPInstanceProvider";
    private final OkHttpClient client;
    private Context context;

    public HTTPInstanceProvider(InstanceAcquisitionListener listener, Context context) {
        super(listener);
        this.context = context;
        client = new OkHttpClient();
    }

    @Override
    public void getNewInstance(final String id) {
        new Thread() {
            @Override
            public void run() {
                InstanceRequest instanceRequest = new InstanceRequest(context, id);
                URL url = instanceRequest.getURL();
                if (url != null) {
                    Request request = new Request.Builder().url(url).build();
                    Response response;
                    try {
                        response = client.newCall(request).execute();

                        if (response.isSuccessful()) {
                            String responseString = response.body().string();
                            Instance instance = null;
                            try {
                                instance = GsonUtil.getGson().fromJson(responseString, Instance.class);
                                listener.onInstanceAcquired(instance);
                            } catch (JsonSyntaxException e) {
                                Log.e(TAG, "Received Instance '" + id + "' with broken syntax", e);
                            }
                        } else {
                            Log.e(TAG, "Cannot retrieve Instance '" + id + "'. Failed with HTTP status code " + response.code());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot retrieve new Instance '" + id + "'", e);
                    }
                }
            }
        }.start();
    }

    @Override
    public void getNewInstances(final String[] IDs) {
        new Thread() {
            @Override
            public void run() {
                //todo request all at once
                for (String id : IDs) {
                    getNewInstance(id);
                }
            }
        }.start();
    }

    @Override
    public void deleteInstance(final String id) {
        new Thread() {
            @Override
            public void run() {
                DeleteRequest deleteRequest = new DeleteRequest(context, id);
                MediaType postMediaType = deleteRequest.getPostMediaType();
                String postBody = deleteRequest.getPOSTData();
                URL url = deleteRequest.getURL();
                if (url != null) {
                    Request request = new Request.Builder()
                            .url(url)
                            .post(RequestBody.create(postMediaType, postBody))
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully deleted Instance '" + id + "'");
                            listener.onInstanceDeleted(id);
                        } else {
                            Log.e(TAG, "Cannot delete Instance '" + id + "'. Failed with HTTP status code " + response.code());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot delete Instance '" + id + "'");
                    }
                }
            }
        }.start();
    }

    @Override
    public void getAvailableInstances() {
        new Thread() {
            @Override
            public void run() {
                InstanceListRequest listRequest = new InstanceListRequest(context);
                URL url = listRequest.getURL();
                if (url != null) {
                    Request request = new Request.Builder().url(url).build();
                    Response response;
                    try {
                        response = client.newCall(request).execute();

                        if (response.isSuccessful()) {
                            String responseString = response.body().string();
                            String instances[] = new String[0];
                            try {
                                instances = GsonUtil.getGson().fromJson(responseString, String[].class);
                            } catch (JsonSyntaxException e) {
                                Log.e(TAG, "Received Instance list with broken syntax", e);
                            }
                            Collection<String> instancesColl = new ArrayList<>();
                            Collections.addAll(instancesColl, instances);
                            listener.onInstanceListAcquired(instancesColl);
                        } else {
                            Log.e(TAG, "Cannot retrieve Instance list. Failed with HTTP status code " + response.code());
                            listener.onInstanceListAcquisitionFailed();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot retrieve Instance list", e);
                        listener.onInstanceListAcquisitionFailed();
                    }
                }
            }
        }.start();
    }
}
