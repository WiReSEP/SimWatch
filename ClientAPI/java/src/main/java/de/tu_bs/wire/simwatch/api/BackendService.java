package de.tu_bs.wire.simwatch.api;


import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Update;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

interface BackendService {

    @POST("/instances")
    Instance register(@Body Instance instance);

    @POST("/instances/{instId}/updates")
    void update(@Path("instId") String instId, @Body Update update);

}
