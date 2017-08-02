package vn.vnpt.ssdc.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.AcsResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thangnc on 14-Jun-17.
 */
@Component
public class RPCApiClient {

    private static final Logger logger = LoggerFactory.getLogger(RPCApiClient.class);

    private static final String NOW_TO_STRING = "now";
    private static final String OBJECT_NAME = "objectName";
    private static final String COMMAND_KEY = "commandKey";

    private RestTemplate restTemplate;
    private String apiEndpointUrl;
    private String apiEndStringBase;

    public static final long MAX_TIMEOUT_WAIT = 60000;
    public static final long TIME_WAIT = 3000;

    @Autowired
    public RPCApiClient(@Value("${apiEndpointUrl}") String apiEndpointUrl,
                        RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiEndStringBase = apiEndpointUrl;
        this.apiEndpointUrl = apiEndpointUrl + "/rpc";
    }

    public AcsResponse reboot(String deviceId, String commandKey, Boolean now) {
        String url = String.format("%s/%s/reboot", apiEndpointUrl, deviceId);
        Map<String, String> data = new HashMap<>();
        data.put(NOW_TO_STRING, now.toString());
        data.put(COMMAND_KEY, commandKey);
        return this.restTemplate.postForObject(url, data, AcsResponse.class);
    }

    public AcsResponse downloadFile(String deviceId, Boolean now, String fileType, String url, String username, String password,
                                    String successUrl, String failureUrl, String commandKey, String fileSize, String targetFileName,
                                    String delaySeconds, String status, String startTime, String completeTime) {
        String urlGetObject = this.apiEndpointUrl + "/" + deviceId + "/downloadFile";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(NOW_TO_STRING, now);
        bodyVariable.put("fileType", fileType);
        bodyVariable.put("url", url);
        bodyVariable.put("username", username);
        bodyVariable.put("password", password);
        bodyVariable.put("successUrl", successUrl);
        bodyVariable.put("failureUrl", failureUrl);
        bodyVariable.put("commandKey", commandKey);
        bodyVariable.put("fileSize", fileSize);
        bodyVariable.put("targetFileName", targetFileName);
        bodyVariable.put("delaySeconds", delaySeconds);
        bodyVariable.put("status", status);
        bodyVariable.put("startTime", startTime);
        bodyVariable.put("completeTime", completeTime);
        return this.restTemplate.postForObject(urlGetObject, bodyVariable, AcsResponse.class, new HashMap<String, String>());
    }

    public AcsResponse uploadFile(String deviceId, Boolean now,String fileType, String url, String username, String password,
                                  String delaySeconds, String commandKey) {
        String urlGetObject = this.apiEndpointUrl + "/" + deviceId + "/uploadFile";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(NOW_TO_STRING, now);
        bodyVariable.put("fileType", fileType);
        bodyVariable.put("url", url);
        bodyVariable.put("username", username);
        bodyVariable.put("password", password);
        bodyVariable.put("delaySeconds", delaySeconds);
        bodyVariable.put("commandKey", commandKey);
        return this.restTemplate.postForObject(urlGetObject, bodyVariable, AcsResponse.class, new HashMap<String, String>());
    }

    public AcsResponse getParamterNames(String deviceId, Boolean now, String parameterPath, String nextLevel) {
        String urlGetParam = this.apiEndpointUrl + "/" + deviceId + "/get-parameter-names";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put(NOW_TO_STRING, now);
        bodyVariable.put("parameterPath", parameterPath);
        bodyVariable.put("nextLevel", nextLevel);
        return this.restTemplate.postForObject(urlGetParam, bodyVariable, AcsResponse.class, new HashMap<String, String>());
    }

}
