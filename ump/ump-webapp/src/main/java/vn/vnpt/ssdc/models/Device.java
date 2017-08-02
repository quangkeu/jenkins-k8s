package vn.vnpt.ssdc.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.api.client.AcsApiClient;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.Parameter;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.utils.DateUtils;
import vn.vnpt.ssdc.utils.IpUtil;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by vietnq on 10/31/16.
 */
public class Device {
    public static final int MAX_NUMBER_PARAMATER_TO_GETINFOR = 5;
    private static final Logger logger = LoggerFactory.getLogger(Device.class);
    public String id;
    public Map<String, List<Tag>> tags = new HashMap<>();
    public Map<String, String> parameters = new HashMap<>(); //holds device (parameter,value) mapping

    private static final String VALUE_KEY = "_value";

    public String serialNumber() {
        return parameters.get("_deviceId._SerialNumber");
    }

    public String manufacturer() {
        return parameters.get("_deviceId._Manufacturer");
    }

    public String oui() {
        return parameters.get("_deviceId._OUI");
    }

    public String productClass() {
        return parameters.get("_deviceId._ProductClass");
    }

    public String softwareVersion() {
        return parameters.get("summary.softwareVersion");
    }

    public String modelName() {
        return parameters.get("summary.modelName");
    }

    public boolean isOnline() {
        Boolean result = true;
        String lastInform = parameters.get("_lastInform");
        if("".equals(parameters.get("summary.periodicInformInterval"))){
            return true;
        }
        int periodicInformInterval = Integer.valueOf(parameters.get("summary.periodicInformInterval"));
        if(lastInform.isEmpty()) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date lastInformTime = sdf.parse(lastInform);
            Date now = new Date();
            if ((now.getTime() - lastInformTime.getTime()) > 1000*periodicInformInterval) {
                result = false;
            }
        } catch (ParseException e) {
            logger.error("Cannot parse time: {}", lastInform);
        }
        return result;
    }

    /**
     * returns a device from a json object
     *
     * @param deviceObject
     * @param paramNames
     * @return
     */
    public static Device fromJsonObject(JsonObject deviceObject, Set<String> paramNames) {
        Device device = new Device();
        device.id = deviceObject.get("_id").getAsString();
        for (String param : paramNames) {

            if ("_tags".equalsIgnoreCase(param)) {
                if(deviceObject.get(param) != null) {
                    ArrayList<String> listTags = new Gson().fromJson(deviceObject.get(param), new TypeToken<ArrayList<String>>() {}.getType());
                    String result= StringUtils.join(listTags, ",");
                    device.parameters.put(param, result);
                }
                else{
                    device.parameters.put(param,"");
                }
            } else {
                device.parameters.put(param, getParamValue(deviceObject, param));
                if ("_registered".equalsIgnoreCase(param) || "_registered".equalsIgnoreCase(param) || "_lastInform".equalsIgnoreCase(param) || "_timestamp".equalsIgnoreCase(param)) {
                    if(!getParamValue(deviceObject, param).isEmpty()) {
                        device.parameters.put(param, DateUtils.convertIsoDateToString(getParamValue(deviceObject, param)));
                    }
                }
                if ("_ip".equalsIgnoreCase(param)) {
                    if (getParamValue(deviceObject, param) != null && !"".equals(getParamValue(deviceObject, param)))
                        device.parameters.put(param, IpUtil.convertIntToIpAddres(Integer.valueOf(getParamValue(deviceObject, param))));
                    else
                        device.parameters.put(param, "");
                }
            }

        }
        return device;
    }

    public static DeviceInfo fromJsonObjectDeviceInfo(JsonObject deviceObject, Set<String> paramNames) {
        DeviceInfo deviceInfo = new DeviceInfo();
        for (String param : paramNames) {
            if ("_tags".equalsIgnoreCase(param)) {
                if(deviceObject.get(param) != null) {
                    ArrayList<String> listTags = new Gson().fromJson(deviceObject.get(param), new TypeToken<ArrayList<String>>() {}.getType());
                    String result= StringUtils.join(listTags, ",");
                    deviceInfo.parameters.put(param, result);
                }
                else{
                    deviceInfo.parameters.put(param,"");
                }
            } else {
                deviceInfo.parameters.put(param, getParamValue(deviceObject, param));
                if ("_registered".equalsIgnoreCase(param) || "_lastInform".equalsIgnoreCase(param)
                        || "_timestamp".equalsIgnoreCase(param)|| "_lastBoot".equalsIgnoreCase(param)) {
                    deviceInfo.parameters.put(param, DateUtils.convertIsoDateToString(getParamValue(deviceObject, param)));

                }
                if ("_lastBoot".equalsIgnoreCase(param)) {
                    if(!getParamValue(deviceObject, param).isEmpty()) {
                        deviceInfo.parameters.put(param, getRelativeTimeDisplay(getParamValue(deviceObject, param)));
                    }

                }
                if ("_ip".equalsIgnoreCase(param)) {
                    if (getParamValue(deviceObject, param) != null && !"".equals(getParamValue(deviceObject, param)))
                        deviceInfo.parameters.put(param, IpUtil.convertIntToIpAddres(Integer.valueOf(getParamValue(deviceObject, param))));
                    else
                        deviceInfo.parameters.put(param, "");
                }
            }
        }
        return deviceInfo;
    }

    /**
     * returns list of devices from a json string <br/>
     * this is used after querying device from acs
     *
     * @param queryResult a json string
     * @param paramNames  set of selected parameters
     * @return a Device object
     */
    public static List<DeviceInfo> fromJsonStringDeviceInfo(String queryResult, Set<String> paramNames) {
        List<DeviceInfo> list = new ArrayList<>();
        JsonArray array = new Gson().fromJson(queryResult, JsonArray.class);
        for (int i = 0; i < array.size(); i++) {
            list.add(fromJsonObjectDeviceInfo(array.get(i).getAsJsonObject(), paramNames));
        }
        return list;
    }


    public static List<Device> fromJsonString(String queryResult, Set<String> paramNames) {
        List<Device> list = new ArrayList<>();
        JsonArray array = new Gson().fromJson(queryResult, JsonArray.class);
        for (int i = 0; i < array.size(); i++) {
            list.add(fromJsonObject(array.get(i).getAsJsonObject(), paramNames));
        }
        return list;
    }

    //returns value of a full path param from a json string
    private static String getParamValue(JsonObject deviceObject, String paramName) {
        String value = "";
        try {
            if (paramName.startsWith("summary")) {
                try {
                    deviceObject = deviceObject.get(paramName).getAsJsonObject();
                } catch (Exception e) {
//                    logger.error("{}", e);
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
//                        logger.error(e.getMessage());
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
//            logger.error("Error when getting value for param: {}, exception: {}", paramName, e.getCause());
            throw e;
        }
        return value;
    }


    public static Map<String, String> getAllValueForTag(String deviceId, Tag tag, AcsApiClient client) {
        Map<String, String> queryMapParam = new HashMap<>();
        Map<String, Parameter> mapPamrater = new HashMap<>();
        mapPamrater.putAll(tag.parameters);
        Map<String, String> mapParamterFinal = new HashMap<>();
        StringBuilder paramTmp = new StringBuilder("");
        while (!mapPamrater.isEmpty()) {
            queryMapParam.put("query", "{\"_id\":\"" + deviceId + "\"}");
            int i = 0;
            Iterator it = mapPamrater.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry pair = (Map.Entry) it.next();
                Parameter param = (Parameter) pair.getValue();
                paramTmp.append(param.path).append(",");
                if (i == MAX_NUMBER_PARAMATER_TO_GETINFOR) {
                    break;
                }
                ++i;
                it.remove(); // avoids a ConcurrentModificationException
            }
            paramTmp.deleteCharAt(paramTmp.length() - 1);
            queryMapParam.put("parameters", paramTmp.toString());
            AcsResponse resParam = client.findDevices(queryMapParam);
            // pass _resTmp
            JsonArray arrayTmpParam = new Gson().fromJson(resParam.body, JsonArray.class);
            JsonObject objectTmpParam = arrayTmpParam.get(0).getAsJsonObject();
            Map<String, String> listFullPath = listJson(objectTmpParam);
            mapParamterFinal.putAll(listFullPath);
        }
        return mapParamterFinal;
    }

    private static Map<String, String> listJson(JsonObject json) {
        Map<String, String> listParam = new HashMap<>();
        listJSONObject("", json, listParam);
        return listParam;
    }

    private static void listObject(String parent, Object data, Map<String, String> listParam) {

        if (data instanceof JsonObject) {
            listJSONObject(parent, (JsonObject) data, listParam);
        } else if (data instanceof JsonArray) {
            listJSONArray(parent, (JsonArray) data, listParam);
        }
    }

    private static void listJSONObject(String parent, JsonObject json, Map<String, String> listParam) {
        for (Map.Entry<String, JsonElement> tmp : json.entrySet()) {
            String key = tmp.getKey();
            if (json.get(key).isJsonObject()) {
                JsonObject child = json.get(key).getAsJsonObject();
                String childKey = parent.isEmpty() ? key : parent + "." + key;
                listObject(childKey, child, listParam);
                if (child.get(VALUE_KEY) != null && !child.get(VALUE_KEY).isJsonObject())
                    listParam.put(childKey, child.get(VALUE_KEY).getAsString());
            }

        }
    }

    private static void listJSONArray(String parent, JsonArray json, Map<String, String> listParam) {
        for (int i = 0; i < json.size(); i++) {
            JsonObject data = json.get(i).getAsJsonObject();
            listObject(parent, data, listParam);
        }
    }


    private static String getInstaceOfpath(String fullPath, String parentPath) {
        String instance = "";
        String tmp = fullPath.replaceFirst(parentPath, "");
        String[] array = tmp.split("\\.");
        if (!"".equalsIgnoreCase(array[0]) && StringUtils.isNumeric(array[0]))
            return array[0];
        return instance;
    }

    public static String getRelativeTimeDisplay(String reboot) {
        try {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            format.setTimeZone(tz);
            Date past = format.parse(reboot);
            Date now = new Date();
            long seconds = (now.getTime() - past.getTime())/1000;
            return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
        }
        catch (Exception j){
            j.printStackTrace();
            return "";
        }
    }
}
