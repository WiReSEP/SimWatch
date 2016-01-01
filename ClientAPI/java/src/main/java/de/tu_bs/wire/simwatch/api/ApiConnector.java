package de.tu_bs.wire.simwatch.api;

import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Update;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Main entry point to access the update API
 */
public class ApiConnector {
    private static ApiConnector instance;
    private final BackendService backendService;
    private Instance simInstance;

    public static ApiConnector getInstance() {
        if (instance == null) {
            instance = new ApiConnector("http://server.ip/");
        }
        return instance;
    }

    private ApiConnector(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        backendService = retrofit.create(BackendService.class);
    }

    public void register(String name, String profile) {
        Instance instance = new Instance();
        instance.name = name;
        instance.profile = profile;
        simInstance = backendService.register(instance);
    }

    public UpdateBuilder buildUpdate() {
        return new UpdateBuilder(this);
    }

    /*package*/ void update(Update update) {
        if (simInstance == null) {
            throw new IllegalStateException("Must register first");
        }
        backendService.update(simInstance._id, update);
    }

}
