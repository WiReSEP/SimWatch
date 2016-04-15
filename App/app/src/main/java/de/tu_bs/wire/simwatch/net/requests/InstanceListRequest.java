package de.tu_bs.wire.simwatch.net.requests;

/**
 * Created by mw on 19.01.16.
 */
public class InstanceListRequest {

    public static final String INSTANCE_LIST_URL = "http://aquahaze.de:5001/instance/ids";

    public InstanceListRequest() {

    }

    public String getURL() {
        return INSTANCE_LIST_URL;
    }
}
