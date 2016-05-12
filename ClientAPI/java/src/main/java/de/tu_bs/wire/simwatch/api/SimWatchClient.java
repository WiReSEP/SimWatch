package de.tu_bs.wire.simwatch.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.api.models.Update;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Main entry point to access the SimWatch update API.
 * <p>
 * You can use {@link SimWatchClient#registerSimulation(String, File)} to register a simulation.
 * A simulation can send updates after registering by calling {@link SimWatchClient#buildUpdate()}
 * on the returned client instance.
 * <h1>Configuration files</h1>
 * The configuration of the client (e.g. host/port of the SimWatch server) is read from a
 * configuration file named <code>simwatch.yaml</code> which must reside in one of the
 * following directories, listed in the order they are searched:
 * <ul>
 * <li> the current working directory
 * <li> ~/.config/simwatch (per-user config)
 * <li> /etc/simwatch (system-wide config)
 * </ul>
 * The file must contain at least the following line:
 * <pre><code>
 * url: http://&lt;host&gt;:&lt;port&gt;
 * </code></pre>
 * If no configuration file was found, an example configuration will be written.
 *
 * @see UpdateBuilder
 */
@SuppressWarnings("WeakerAccess") // intentionally contains public api methods
public class SimWatchClient {
    private static final Logger LOG = Logger.getLogger(SimWatchClient.class.getSimpleName());
    private static final String CONF_FILE_NAME = "simwatch.yaml";

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$s [SimWatch] %5$s%6$s%n");
    }

    private final BackendService backendService;
    private Instance simInstance;

    private SimWatchClient(Configuration configuration) {
        OkHttpClient client = new OkHttpClient();
        if (configuration.isRequestLoggingEnabled()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(
                    new HttpLoggingInterceptor.Logger() {
                        @Override
                        public void log(String message) {
                            LOG.info(message);
                        }
                    });
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            client.interceptors().add(interceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(configuration.getUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        backendService = retrofit.create(BackendService.class);
    }

    /**
     * Register a simulation.
     * <p>
     * This method returns a client that can be used to send updates.
     *
     * @param name        Name of the simulation, displayed in the app
     * @param profileFile File that contains the simulation profile (JSON)
     * @return A client that can be used to send updates from the simulation
     * @throws RegistrationException if there was an error (e.g. connection issues)
     * @see SimWatchClient#buildUpdate()
     */
    public static SimWatchClient registerSimulation(String name, File profileFile)
            throws RegistrationException {
        Path configFile = findConfigFile();
        if (configFile == null) {
            createExampleConfiguration();
            throw new RegistrationException("No configuration file");
        } else {
            Configuration configuration = readConfiguration(configFile);
            SimWatchClient instance = new SimWatchClient(configuration);
            instance.register(name, profileFile);
            return instance;
        }
    }

    /**
     * Reads and validates the configuration file
     *
     * @param configFile Path to the configuration file to read
     * @return The configuration
     * @throws RegistrationException If reading or parsing the file failed, or if it is invalid
     */
    private static Configuration readConfiguration(Path configFile) throws RegistrationException {
        try (InputStream in = Files.newInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Configuration configuration = yaml.loadAs(in, Configuration.class);
            if (configuration.getUrl() == null) {
                LOG.severe("The config file " + configFile + " does not contain " +
                        "the mandatory 'url' property");
                throw new RegistrationException("url not set in configuration");
            }
            LOG.info(configuration.toString());
            return configuration;
        } catch (IOException | YAMLException e) {
            throw new RegistrationException("Cannot read configuration file", e);
        }
    }

    /**
     * Extracts the example configuration from the resources to the user-specific
     * SimWatch config directory
     */
    private static void createExampleConfiguration() {
        try (InputStream defaultConf =
                     SimWatchClient.class.getResourceAsStream("/" + CONF_FILE_NAME)) {
            Files.createDirectories(ConfigDirectory.USER.path);
            Files.copy(defaultConf, ConfigDirectory.USER.path.resolve("example_" + CONF_FILE_NAME),
                    StandardCopyOption.REPLACE_EXISTING);
            LOG.severe("No configuration file found. " +
                    "An example configuration file has been written to " +
                    ConfigDirectory.USER.path);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "cannot create example config", e);
        }
    }

    /**
     * Searches all configuration directories for a config file named {@link #CONF_FILE_NAME}
     *
     * @return The path to the configuration file that should be used
     * @see ConfigDirectory
     */
    @Nullable
    private static Path findConfigFile() {
        for (ConfigDirectory dir : ConfigDirectory.values()) {
            Path file = dir.path.resolve(CONF_FILE_NAME);
            if (Files.exists(file)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Register a new simulation
     *
     * @param name        Name of the simulation, displayed in the app
     * @param profileFile File that contains the simulation profile
     * @throws RegistrationException if there was an error (e.g. connection issues)
     */
    private void register(String name, File profileFile) throws RegistrationException {
        try {
            InputStreamReader jsonReader = new FileReader(profileFile);
            Profile profile = new Gson().fromJson(jsonReader, Profile.class);
            Instance instance = new Instance(name, profile);
            Call<Instance> register = backendService.register(instance);
            Response<Instance> response = register.execute();
            if (response.isSuccess()) {
                simInstance = response.body();
            } else {
                String message = format("Server responded with Error %d: %s",
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
            throw new IllegalStateException("Not registered");
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
                        LOG.warning(format("Cannot upload attachment '%s': Error %d - %s",
                                propertyName, uploadResp.code(), uploadResp.message()));
                        return false;
                    }
                }
                return true;
            } else {
                LOG.warning(format("Cannot post update: Error %d - %s",
                        response.code(), response.message()));
                return false;
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot post update", e);
            return false;
        }
    }

    /**
     * Enum that specifies all directories that are searched for a
     * configuration file.
     * They are searched in the order they are declared.
     */
    private enum ConfigDirectory {
        CWD(Paths.get(".").toAbsolutePath().normalize()),
        USER(Paths.get(System.getProperty("user.home"), ".config", "simwatch")),
        SYSTEM(Paths.get("/etc", "simwatch"));

        public final Path path;

        ConfigDirectory(Path path) {
            this.path = path;
        }
    }

}
