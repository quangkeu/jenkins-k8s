package vn.vnpt.ssdc.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import vn.vnpt.ssdc.utils.DateUtils;
import vn.vnpt.ssdc.utils.IpUtil;

import java.util.*;

/**
 * Created by Lamborgini on 3/6/2017.
 */
public class FileManagement {

    private static final String VALUE_KEY = "_value";
    public Map<String, String> parameters = new HashMap<>();
    public String id;
    public String fileName;
    private static final long MEGABYTE = 1024L * 1024L;


    public static List<FileManagement> fromJsonString(String queryResult, Set<String> paramNames) {
        List<FileManagement> list = new ArrayList<>();
        JsonArray array = new Gson().fromJson(queryResult, JsonArray.class);
        for (int i = 0; i < array.size(); i++) {
            list.add(fromJsonObject(array.get(i).getAsJsonObject(), paramNames));
        }
        return list;
    }

    public static FileManagement fromJsonObject(JsonObject deviceObject, Set<String> paramNames) {
        FileManagement fileManagement = new FileManagement();
        fileManagement.id = deviceObject.get("_id").getAsString();
//        fileManagement.fileName = deviceObject.get("metadata.uploadFilename").getAsString();
        for (String param : paramNames) {

            if ("uploadDate".equalsIgnoreCase(param)) {
                fileManagement.parameters.put(param, getParamValue(deviceObject, param).replace("T", " "));
            } else if ("length".equalsIgnoreCase(param)) {
                fileManagement.parameters.put(param, String.valueOf(bytesToMeg(Long.valueOf(getParamValue(deviceObject, param)))));
            }
//            else if ("metadata.uploadFileName".equalsIgnoreCase(param)) {
//                fileManagement.fileName = getParamValue(deviceObject, param);
//            }
            else {fileManagement.parameters.put(param, getParamValue(deviceObject, param));}

        }
        return fileManagement;
    }

    private static String getParamValue(JsonObject deviceObject, String paramName) {
        String value = "";
        try {
            if (paramName.startsWith("summary")) {
                try {
                    deviceObject = deviceObject.get(paramName).getAsJsonObject();
                } catch (Exception e) {
                    if (deviceObject.get(paramName) != null) {
                        value = deviceObject.get(paramName).getAsString();
                    } else {
                        value = "";
                    }
                }
            } else {
                String[] parts = paramName.split("\\."); //need escape to have correct regex here
                for (int i = 0; i < parts.length; i++) {
                    try {
                        deviceObject = deviceObject.getAsJsonObject(parts[i]);
                    } catch (Exception e) {
                        value = deviceObject.get(parts[i]).getAsString();
                    }
                }
            }

            if ("".equals(value)) {
                if (deviceObject != null && deviceObject.has(VALUE_KEY)) {
                    value = deviceObject.get(VALUE_KEY).getAsString();
                }
                if (paramName.endsWith(".")) {
                    value = new Gson().toJson(deviceObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static long bytesToMeg(long bytes) {
        return bytes / MEGABYTE;
    }

    public String oui() {
        return parameters.containsKey("metadata.oui") ? parameters.get("metadata.oui") : null;
    }

    public String productClass() {
        return parameters.containsKey("metadata.productClass") ? parameters.get("metadata.productClass") : null;
    }

    public String version() {
        return parameters.containsKey("metadata.version") ? parameters.get("metadata.version") : null;
    }

    public String fileType() {
        return parameters.containsKey("metadata.fileType") ? parameters.get("metadata.fileType") : null;
    }
}
