package de.tu_bs.wire.simwatch.api;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Update;
import retrofit.Call;
import retrofit.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

interface BackendService {

    @POST("/instance")
    Call<Instance> register(@Body Instance instance);

    @POST("/instance/{instId}/updates")
    Call<Void> update(@Path("instId") String instId, @Body Update update);

}
