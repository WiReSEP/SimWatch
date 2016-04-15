package de.tu_bs.wire.simwatch.net.requests;

import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;

/**
 * Created by mw on 22.03.16.
 */
public class DeleteRequest {

    public static final String DELETE_URL = "http://aquahaze.de:5001/instance/delete";
    public static final MediaType POST_MEDIA_TYPE = MediaType.parse("application/json");
    private static final String DELETE_PROPERTY_NAME = "_id";
    private String instanceID;


    public DeleteRequest(String instanceID) {
        this.instanceID = instanceID;
    }

    public String getURL() {
        return DELETE_URL;
    }

    public MediaType getPostMediaType() {
        return POST_MEDIA_TYPE;
    }

    public String getPOSTData() {
        JsonObject postObject = new JsonObject();
        postObject.addProperty(DELETE_PROPERTY_NAME, instanceID);
        return postObject.toString();
    }
}
