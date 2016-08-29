package de.tu_bs.wire.simwatch.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by mw on 19.08.16.
 */
public class GsonUtil {

    private static Gson gson = null;

    public static Gson getGson() {
        if (gson == null) {
            final GsonBuilder gsonBuilder = getBuilder();
            gson = gsonBuilder.create();
        }
        return gson;
    }

    public static GsonBuilder getBuilder(GsonBuilder gsonBuilder) {
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        return gsonBuilder;
    }

    public static GsonBuilder getBuilder() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        return getBuilder(gsonBuilder);
    }

}
