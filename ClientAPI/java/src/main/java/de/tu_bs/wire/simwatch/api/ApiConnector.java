package de.tu_bs.wire.simwatch.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.api.models.Update;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point to access the update API
 */
public class ApiConnector {
    private static final Logger LOG = Logger.getLogger(ApiConnector.class.getName());
    private static ApiConnector instance;
    private final BackendService backendService;
    private Instance simInstance;

    public static ApiConnector getInstance() {
        if (instance == null) {
            instance = new ApiConnector("http://aquahaze.de:5001/");
        }
        return instance;
    }

    private ApiConnector(String baseUrl) {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        client.interceptors().add(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        backendService = retrofit.create(BackendService.class);
    }

    public void register(String name, File profileFile) throws RegistrationException {
        try {
            InputStreamReader jsonReader = new FileReader(profileFile);
            Profile profile = new Gson().fromJson(jsonReader, Profile.class);
            Instance instance = new Instance(name, profile);
            Call<Instance> register = backendService.register(instance);
            Response<Instance> response = register.execute();
            if (response.isSuccess()) {
                simInstance = response.body();
            } else {
                String message = String.format("Server responded with Error %d: %s",
                        response.code(), response.message());
                throw new RegistrationException(message);
            }
        } catch (JsonSyntaxException | IOException e) {
            throw new RegistrationException(e);
        }
    }

    public UpdateBuilder buildUpdate() {
        return new UpdateBuilder(this);
    }

    /*package*/ boolean update(Update update) {
        if (simInstance == null) {
            throw new IllegalStateException("Must register first");
        }
        try {
            Response response = backendService.update(simInstance.getID(), update).execute();
            if (response.isSuccess()) {
                return true;
            } else {
                LOG.log(Level.WARNING, "Cannot post update: Error " + response.code()
                        + " " + response.message());
                return false;
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot post update", e);
            return false;
        }
    }

}
