package de.tu_bs.wire.simwatch.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point to access the SimWatch update API.
 * <p>
 * You can use the instance returned by {@link ApiConnector#getInstance()} to register a simulation via
 * {@link ApiConnector#register(java.lang.String, java.io.File)}. A simulation can send updates after
 * registering by calling {@link ApiConnector#buildUpdate()} (See {@link UpdateBuilder}).
 */
@SuppressWarnings("WeakerAccess") // intentionally contains public api methods
public class ApiConnector {
    private static final Logger LOG = Logger.getLogger(ApiConnector.class.getName());
    private static ApiConnector instance;
    private final BackendService backendService;
    private Instance simInstance;

    // todo: this should register an instance and return different instances of the connector
    // todo the backend ip must be configurable
    public static ApiConnector getInstance() {
        if (instance == null) {
            instance = new ApiConnector("http://localhost:5000/");
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

    /**
     * Register a new simulation,
     *
     * @param name        Name of the simulation, displayed in the app
     * @param profileFile File that contains the simulation profile
     * @throws RegistrationException
     */
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

    /**
     * Entry point for sending simulation updates. Constructs an {@link UpdateBuilder}
     *
     * @return A new {@link UpdateBuilder}
     */
    public UpdateBuilder buildUpdate() {
        return new UpdateBuilder(this);
    }

    /**
     * Send an update object and all attachments to the backend.
     *
     * @param update      The update object
     * @param attachments Attachments to upload
     * @return whether the update was successfull
     */
    /*package*/ boolean update(Update update, Map<String, RequestBody> attachments) {
        if (simInstance == null) {
            throw new IllegalStateException("Must register first");
        }
        try {
            Response response = backendService.update(simInstance.getID(), update).execute();
            if (response.isSuccess()) {
                for (Map.Entry<String, RequestBody> attachment : attachments.entrySet()) {
                    String propertyName = attachment.getKey();
                    RequestBody requestBody = attachment.getValue();
                    Response<Void> uploadResp =
                            backendService.uploadAttachment(simInstance.getID(), propertyName,
                                    requestBody).execute();
                    if (!uploadResp.isSuccess()) {
                        LOG.log(Level.WARNING,
                                String.format("Cannot upload attachment '%s': Error %d - %s",
                                        propertyName, uploadResp.code(), uploadResp.message()));
                        return false;
                    }
                }
                return true;
            } else {
                LOG.log(Level.WARNING, String.format("Cannot post update: Error %d - %s",
                        response.code(), response.message()));
                return false;
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot post update", e);
            return false;
        }
    }

}
