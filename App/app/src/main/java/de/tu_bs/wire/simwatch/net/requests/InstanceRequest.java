package de.tu_bs.wire.simwatch.net.requests;

/**
 * Created by mw on 19.01.16.
 */
public class InstanceRequest {

    public static final String INSTANCE_URL = "http://aquahaze.de:5001/instance/%s";

    private String id;

    public InstanceRequest(String id) {
        this.id = id;
    }

    public String getURL() {
        return String.format(INSTANCE_URL, id);
    }
}
