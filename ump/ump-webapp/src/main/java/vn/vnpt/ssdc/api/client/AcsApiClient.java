package vn.vnpt.ssdc.api.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.utils.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by vietnq on 10/31/16.
 */
@Component
public class AcsApiClient {

    private static final Logger logger = LoggerFactory.getLogger(AcsApiClient.class);

    private static final String NOW_TO_STRING = "now";
    private static final String OBJECT_NAME = "objectName";

    private RestTemplate restTemplate;
    private String apiEndpointUrl;
    private String apiEndStringBase;

    public static final long MAX_TIMEOUT_WAIT = 60000;
    public static final long TIME_WAIT = 3000;


    @Autowired
    protected HttpSession session;

    @Autowired
    public AcsApiClient(@Value("${apiEndpointUrl}") String apiEndpointUrl,
                        RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiEndStringBase = apiEndpointUrl;
        this.apiEndpointUrl = apiEndpointUrl + "/acs";
    }

    public AcsResponse findDevices(Map<String, String> queryParams) {
        String permissionQuery = session.getAttribute("query").toString();
        String query = permissionQuery;
        if(queryParams.get("query") != null){
            query = "{\"$and\":["+queryParams.get("query")+","+permissionQuery+"]}";
        }
        queryParams.put("query",query);
        String queryString = StringUtils.queryStringFromMap(queryParams);
        String url = this.apiEndpointUrl + "?" + queryString;
        return this.restTemplate.getForObject(url, AcsResponse.class, queryParams);
    }

    public AcsResponse findDevicesOld(Map<String, String> queryParams) {
        String queryString = StringUtils.queryStringFromMap(queryParams);
        String url = this.apiEndpointUrl + "?" + queryString;
        return this.restTemplate.getForObject(url, AcsResponse.class, queryParams);
    }

    public AcsResponse setParameterValues(String deviceId, Map<String, Object> paramValues) {
        String url = String.format("%s/%s/set-parameter-values", apiEndpointUrl, deviceId);
        return this.restTemplate.postForObject(url, paramValues, AcsResponse.class);
    }


    /**
     * Reboot device by device ID
     *
     * @param deviceId
     * @param now      String "true" or "false"
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    public AcsResponse reboot(String deviceId, Boolean now) {
        String url = String.format("%s/%s/reboot?now=%s", apiEndpointUrl, deviceId, now.toString());
        Map<String, String> data = new HashMap<>();
        return this.restTemplate.postForObject(url, data, AcsResponse.class);
    }

    /**
     * Factory device by device ID
     *
     * @param deviceId
     * @param now      String "true" or "false"
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    public AcsResponse factoryReset(String deviceId, Boolean now) {
        String url = String.format("%s/%s/factory-reset?now=%s", apiEndpointUrl, deviceId, now.toString());
        Map<String, String> data = new HashMap<>();
        return this.restTemplate.postForObject(url, data, AcsResponse.class);
    }

    /**
     * add Object to device
     *
     * @param deviceId
     * @param now
     * @param objectName
     * @param parameterValues
     * @return
     */
    public AcsResponse addObject(String deviceId, Boolean now, String objectName, Map<String, String> parameterValues) {
        String url = String.format("%s/%s/add-object?now=%s", apiEndpointUrl, deviceId, now.toString());
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(OBJECT_NAME, objectName);
        bodyVariable.put("parameterValues", parameterValues);
        return this.restTemplate.postForObject(url, bodyVariable, AcsResponse.class);
    }


    public AcsResponse deleteObject(String deviceId, Boolean now, String objectName) {
        String url = String.format("%s/%s/delete-object?now=%s", apiEndpointUrl, deviceId, now.toString());
        JsonObject object = new JsonObject();
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(OBJECT_NAME, objectName);
        return this.restTemplate.postForObject(url, bodyVariable, AcsResponse.class);
    }

    public AcsResponse getParamterValues(String deviceId, List<String> listParameter) {
        String urlGetParam = this.apiEndpointUrl + "/" + deviceId + "/get-parameter-values";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(NOW_TO_STRING, Boolean.TRUE);
        bodyVariable.put("parameters", listParameter);
        return this.restTemplate.postForObject(urlGetParam, bodyVariable, AcsResponse.class, new HashMap<String, String>());
    }

    public AcsResponse refreshObject(String deviceId, String object) {
        String urlGetObject = this.apiEndpointUrl + "/" + deviceId + "/refresh-object";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(NOW_TO_STRING, Boolean.TRUE);
        bodyVariable.put(OBJECT_NAME, object);
        return this.restTemplate.postForObject(urlGetObject, bodyVariable, AcsResponse.class, new HashMap<String, String>());
    }

    public AcsResponse downloadFile(String deviceId, Boolean now, String fileId,String fileName) {
        String urlGetObject = this.apiEndpointUrl + "/" + deviceId + "/downloadFile";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(NOW_TO_STRING, now);
        bodyVariable.put("fileId", fileId);
        bodyVariable.put("fileName", fileName);
        return this.restTemplate.postForObject(urlGetObject, bodyVariable, AcsResponse.class, new HashMap<String, String>());
    }

    public AcsResponse uploadFile(String deviceId, Boolean now,String fileType) {
        String urlGetObject = this.apiEndpointUrl + "/" + deviceId + "/uploadFile";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(NOW_TO_STRING, now);
        bodyVariable.put("fileType", fileType);
        return this.restTemplate.postForObject(urlGetObject, bodyVariable, AcsResponse.class, new HashMap<String, String>());
    }

    /**
     * wait 1 minutes to get data from db
     *
     * @return
     */
    public boolean loopWaitResult(AcsResponse response) {
        if (response.httpResponseCode == 200) {
            return true;
        } else if (response.httpResponseCode == 202) {
            JsonObject object = new Gson().fromJson(response.body, JsonObject.class);
            String idTask = object.get("_id").getAsString();
            String urlCheckExist = apiEndStringBase + "/tasks/checkExist/" + idTask;
            AcsResponse responseTemp = this.restTemplate.getForObject(urlCheckExist, AcsResponse.class, new HashMap<String, String>());
            long startTime = System.currentTimeMillis();
            while (responseTemp.nbOfItems > 0) {
                try {
                    Thread.sleep(TIME_WAIT);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
                responseTemp = this.restTemplate.getForObject(urlCheckExist, AcsResponse.class, new HashMap<String, String>());
                if (System.currentTimeMillis() - startTime > MAX_TIMEOUT_WAIT) {
                    return false;
                }
            }

        } else return false;


        return true;
    }

    public boolean deleteDevice(String deviceId) {
        try{
            String url = this.apiEndpointUrl + "/{deviceId}" + "/delete-device";
            Map<String, String> data = new HashMap<>();
            data.put("deviceId", deviceId);
            this.restTemplate.delete(url, data);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public int addLabel(String deviceId, String label) {
        String url = String.format("%s/%s/create-label/%s", apiEndpointUrl, deviceId, label);
        Map<String, String> data = new HashMap<>();
        return this.restTemplate.postForObject(url, data, Integer.class);
    }

    public boolean deleteLabel(String device, String label) {
        String url = String.format("%s/%s/remove-label/%s", apiEndpointUrl, device, label);
        Map<String, String> data = new HashMap<>();
        return this.restTemplate.postForObject(url, data, Boolean.class);
    }



    public AcsResponse getAllFiles(Map<String, String> queryParams) {
        String queryString = StringUtils.queryStringFromMap(queryParams);
        String url = apiEndpointUrl + "/get-all-files?" + queryString;
        return this.restTemplate.getForObject(url, AcsResponse.class, queryParams);
    }

    public boolean recheckStatus(String deviceId) {
        String url = String.format("%s/recheck-status/%s", apiEndpointUrl, deviceId);
        Map<String, String> data = new HashMap<>();
        return this.restTemplate.postForObject(url, data, Boolean.class);
    }

    public boolean checkObject(String deviceId, String object) {
        String url = String.format("%s/%s/%s/checkObject", apiEndpointUrl, deviceId, object);
        return this.restTemplate.getForObject(url, Boolean.class);
    }

    public AcsResponse getDevices(String serial) {
        String url = String.format("%s/getDevices?serial=%s",
                this.apiEndpointUrl, serial);
        return this.restTemplate.getForObject(url, AcsResponse.class);
    }
}
