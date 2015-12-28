package de.tu_bs.wire.simwatch.api;

/**
 * Main entry point to access the update API
 */
public class ApiConnector {
    private static ApiConnector instance;

    public static ApiConnector getInstance() {
        if (instance == null) {
            instance = new ApiConnector();
        }
        return instance;
    }

}
