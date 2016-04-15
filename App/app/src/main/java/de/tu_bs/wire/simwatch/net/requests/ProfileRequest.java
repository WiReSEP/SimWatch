package de.tu_bs.wire.simwatch.net.requests;

/**
 * Created by mw on 09.02.16.
 */
public class ProfileRequest {

    public static final String PROFILE_URL = "http://aquahaze.de:5001/profile/%s";

    private String id;

    public ProfileRequest(String id) {
        this.id = id;
    }

    public String getURL() {
        return String.format(PROFILE_URL, id);
    }
}
