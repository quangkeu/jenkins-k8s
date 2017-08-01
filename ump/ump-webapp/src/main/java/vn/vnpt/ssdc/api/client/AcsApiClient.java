package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.dto.AcsResponse;
import vn.vnpt.ssdc.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vietnq on 10/31/16.
 */
@Component
public class AcsApiClient {

    private RestTemplate restTemplate;
    private String apiEndpointUrl;

    @Autowired
    public AcsApiClient(@Value("${apiEndpointUrl}") String apiEndpointUrl,
                        RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiEndpointUrl = apiEndpointUrl + "acs";
    }

    public AcsResponse findDevices(Map<String, String> queryParams) {
        String queryString = StringUtils.queryStringFromMap(queryParams);
        String url = this.apiEndpointUrl + "?" + queryString;
        return this.restTemplate.getForObject(url, AcsResponse.class, queryParams);
    }

    /**
     * Reboot device by device ID
     *
     * @param deviceId
     * @param timeout
     * @param isNow
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    public AcsResponse reboot(String deviceId, String timeout, String isNow) {
        StringBuilder uri = new StringBuilder(apiEndpointUrl);
        uri.append("/" + deviceId + "/reboot");

        //Build query params
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("now", isNow);
        queryParams.put("timeout", timeout);
        String queryString = StringUtils.queryStringFromMap(queryParams);
        uri.append("?" + queryString);
        return this.restTemplate.postForObject(uri.toString(), null, AcsResponse.class, queryParams);
    }

    /**
     * Factory device by device ID
     *
     * @param deviceId
     * @param timeout
     * @param isNow
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    public AcsResponse factoryReset(String deviceId, String timeout, String isNow) {
        StringBuilder uri = new StringBuilder(apiEndpointUrl);
        uri.append("/" + deviceId + "/factoryReset");

        //Build query params
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("timeout", timeout + "");
        queryParams.put("now", isNow + "");
        String queryString = StringUtils.queryStringFromMap(queryParams);
        uri.append("?" + queryString);
        return this.restTemplate.postForObject(uri.toString(), null, AcsResponse.class, queryParams);
    }
}
