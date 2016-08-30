package de.tu_bs.wire.simwatch.net;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tu_bs.wire.simwatch.api.GsonUtil;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.InstanceStatus;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.net.requests.StatusUpdateRequest;
import de.tu_bs.wire.simwatch.net.requests.UpdateRequest;
import de.tu_bs.wire.simwatch.simulation.UpdateListener;

/**
 * UpdateProvider using HTTP. This should be standard
 */
public class HTTPUpdateProvider extends UpdateProvider {

    private static final String TAG = "HTTPUpdateProvider";
    private final Context context;
    private OkHttpClient client;

    public HTTPUpdateProvider(UpdateListener listener, Context context) {
        super(listener);
        this.context = context;
        client = new OkHttpClient();
    }

    @Override
    public void update(Instance sim) {
        Collection<Instance> sims = new ArrayList<>(1);
        sims.add(sim);
        update(sims);
    }

    @Override
    public void update(final Collection<Instance> sims) {
        new Thread() {
            @Override
            public void run() {
                Set<String> updatedInstances = new HashSet<>();
                Set<String> erroneousUpdates = new HashSet<>();
                for (Instance sim : sims) {
                    UpdateRequest updateRequest = new UpdateRequest(context, sim);
                    URL url = updateRequest.getURL();
                    if (url != null) {
                        Request request = new Request.Builder().url(url).build();
                        Response response;
                        try {
                            response = client.newCall(request).execute();

                            if (response.isSuccessful()) {
                                String responseString = response.body().string();
                                Update responseArray[];
                                try {
                                    responseArray = GsonUtil.getGson().fromJson(responseString, Update[].class);
                                    List<Update> updates = Arrays.asList(responseArray);
                                    if (applyUpdates(sim, updates)) {
                                        updatedInstances.add(sim.getID());
                                    }
                                } catch (JsonSyntaxException e) {
                                    Log.e(TAG, "Received Update for '" + sim.getID() + "' with broken syntax", e);
                                    erroneousUpdates.add(sim.getID());
                                }
                            } else {
                                Log.e(TAG, "Cannot retrieve new Updates for '" + sim.getID() + "'. Failed with HTTP status code " + response.code());
                                Log.d(TAG, "Request was " + request.urlString());
                                erroneousUpdates.add(sim.getID());
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Cannot retrieve new Updates for '" + sim.getID(), e);
                            erroneousUpdates.add(sim.getID());
                        }
                    }
                    StatusUpdateRequest statusUpdateRequest = new StatusUpdateRequest(context, sim);
                    URL statusUrl = statusUpdateRequest.getURL();
                    if (statusUrl != null) {
                        Request request = new Request.Builder().url(statusUrl).build();
                        Response response;
                        try {
                            response = client.newCall(request).execute();

                            if (response.isSuccessful()) {
                                String responseString = response.body().string();
                                InstanceStatus instanceStatus;
                                try {
                                    instanceStatus = GsonUtil.getGson().fromJson(responseString, InstanceStatus.class);
                                    if (applyStatusUpdate(sim, instanceStatus) && !erroneousUpdates.contains(sim.getID())) {
                                        updatedInstances.add(sim.getID());
                                    }
                                } catch (JsonSyntaxException e) {
                                    Log.e(TAG, "Received status update for '" + sim.getID() + "' with broken syntax", e);
                                    erroneousUpdates.add(sim.getID());
                                }
                            } else {
                                Log.e(TAG, "Cannot retrieve status update for '" + sim.getID() + "'. Failed with HTTP status code " + response.code());
                                Log.d(TAG, "Request was " + request.urlString());
                                erroneousUpdates.add(sim.getID());
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Cannot retrieve status update for '" + sim.getID(), e);
                            erroneousUpdates.add(sim.getID());
                        }
                    }
                }
                if (erroneousUpdates.size() == sims.size()) {
                    listener.onUpdateFailed(erroneousUpdates);
                } else {
                    if (updatedInstances.isEmpty()) {
                        listener.onNoUpdates();
                    } else {
                        listener.onUpdate(updatedInstances);
                    }
                    if (!erroneousUpdates.isEmpty()) {
                        listener.onUpdateFailed(erroneousUpdates);
                    }
                }
            }
        }.start();
    }
}
