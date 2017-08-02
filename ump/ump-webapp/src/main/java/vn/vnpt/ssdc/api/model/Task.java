package vn.vnpt.ssdc.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.utils.DateUtils;

import java.util.*;

/**
 * Created by SSDC on 11/11/2016.
 */
public class Task extends SsdcEntity<Long> {

    private static final Logger logger = LoggerFactory.getLogger(Task.class);

    public String id;

    public Map<String, String> parameters = new HashMap<>(); //holds device (parameter,value) mapping

    public static List<Task> fromJsonString(String queryResult, Set<String> paramNames) {
        List<Task> list = new ArrayList<>();
        JsonArray array = new Gson().fromJson(queryResult, JsonArray.class);
        for (int i = 0; i < array.size(); i++) {
            list.add(fromJsonObject(array.get(i).getAsJsonObject(), paramNames));
        }
        return list;
    }

    public static Task fromJsonObject(JsonObject deviceObject, Set<String> paramNames) {
        Task task = new Task();
        task.id = deviceObject.get("_id").getAsString();
        for (String param : paramNames) {
            task.parameters.put(param, getParamValue(deviceObject, param));
            if ("timestamp".equalsIgnoreCase(param)) {
                String date = DateUtils.convertIsoDateToString(getParamValue(deviceObject, param));
                task.parameters.put(param, date);
            }

        }
        return task;
    }

    private static String getParamValue(JsonObject deviceObject, String paramName) {
        try {
            String[] parts = paramName.split("\\."); //need escape to have correct regex here
            String value = "";
            for (int i = 0; i < parts.length; i++) {
                if (deviceObject != null && deviceObject.get(parts[i]) != null) {
                    if (i == parts.length - 1) {
                        value = deviceObject.get(parts[i]).getAsString();
                    } else {
                        deviceObject = deviceObject.getAsJsonObject(parts[i]);
                    }
                }
            }
            return value;
        } catch (Exception e) {
            logger.error("Error when getting value form param: {}", paramName, e);
            return "";
        }
    }

    public static Map<String, String> indexParamTasks() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", "Task");
        hashMap.put("device", "DeviceId");
        hashMap.put("timestamp", "Time");
        hashMap.put("fault.detail.Fault.FaultString", "Error");
        hashMap.put("_id", "ID");
        hashMap.put("retries", "Retries");
        return hashMap;
    }

}
