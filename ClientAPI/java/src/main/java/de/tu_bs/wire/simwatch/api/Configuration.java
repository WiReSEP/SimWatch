package de.tu_bs.wire.simwatch.api;

@SuppressWarnings("WeakerAccess") // SnakeYaml needs public setters and constructors
public class Configuration {
    private String url;
    private boolean requestLoggingEnabled;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRequestLoggingEnabled() {
        return requestLoggingEnabled;
    }

    public void setRequestLoggingEnabled(boolean requestLoggingEnabled) {
        this.requestLoggingEnabled = requestLoggingEnabled;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "url='" + url + '\'' +
                ", requestLoggingEnabled=" + requestLoggingEnabled +
                '}';
    }
}
