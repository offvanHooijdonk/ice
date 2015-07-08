package by.ingman.ice.retailerrequest.v2.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by off on 08.07.2015.
 */
public class GsonHelper {
    private static GsonBuilder gsonBuilder = new GsonBuilder();

    public static Gson createGson() {
        return gsonBuilder.create();
    }
}
