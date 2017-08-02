package vn.vnpt.ssdc.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vietnq on 10/31/16.
 */
public class AcsResponse {
    public int nbOfItems; //total number of search result
    public int httpResponseCode;
    public String body;
    public int totalCount;

    public Map<String, String> mapResult = new HashMap<>();

    public AcsResponse() {
    }
}
