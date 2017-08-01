package vn.vnpt.ssdc.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by vietnq on 10/31/16.
 */
public class Device {

    public String id;

    public Map<String,String> parameters = new HashMap<String,String>(); //holds device (parameter,value) mapping

    /**
     * returns a device from a json object
     * @param deviceObject
     * @param paramNames
     * @return
     */
    public static Device fromJsonObject(JsonObject deviceObject, Set<String> paramNames) {
        Device device = new Device();
        device.id = deviceObject.get("_id").getAsString();
        for(String param : paramNames) {
            device.parameters.put(param,getParamValue(deviceObject,param));
        }
        return device;
    }

    /**
     * returns list of devices from a json string <br/>
     * this is used after querying device from acs
     * @param queryResult a json string
     * @param paramNames set of selected parameters
     * @return a Device object
     */
    public static List<Device> fromJsonString(String queryResult, Set<String> paramNames) {
        List<Device> list = new ArrayList<Device>();
        JsonArray array = new Gson().fromJson(queryResult, JsonArray.class);
        for(int i = 0; i < array.size(); i++) {
            list.add(fromJsonObject(array.get(i).getAsJsonObject(),paramNames));
        }
        return list;
    }

    //returns value of a full path param from a json string
    //TODO verify all cases and improve
    private static String getParamValue(JsonObject deviceObject, String paramName) {
        String[] parts = paramName.split("\\."); //need escape to have correct regex here
        String value = "";
        for(int i = 0; i < parts.length; i++) {
            if(i == parts.length - 1) {
                value = deviceObject.get(parts[i]).getAsString();
            } else {
                deviceObject = deviceObject.getAsJsonObject(parts[i]);
            }
        }
        return value;
    }
}
