package de.tu_bs.wire.simwatch.api;

import com.squareup.okhttp.RequestBody;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Update;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

interface BackendService {

    @POST("/instance")
    Call<Instance> register(@Body Instance instance);

    @GET("/instance/{id}")
    Call<Instance> getInstanceById(@Path("id") String id);

    @POST("/instance/{instId}/updates")
    Call<Void> update(@Path("instId") String instId, @Body Update update);

    @POST("/instance/{instId}/attachment/{name}")
    Call<Void> uploadAttachment(@Path("instId") String instId,
                                @Path("name") String name,
                                @Body RequestBody body);

    @POST("/instance/{instId}/status")
    Call<Void> updateStatus(@Path("instId") String instId, @Body StatusUpdate statusUpdate);

    class StatusUpdate {
        final Instance.Status status;
        final Instance.Error error;

        StatusUpdate(Instance.Status status, Instance.Error error) {
            this.status = status;
            this.error = error;
        }
    }
}
