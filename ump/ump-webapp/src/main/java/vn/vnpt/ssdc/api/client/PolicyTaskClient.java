package vn.vnpt.ssdc.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.LoggingDeviceActivity;
import vn.vnpt.ssdc.api.model.PolicyTask;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by THANHLX on 4/17/2017.
 */
@Component
public class PolicyTaskClient extends GenericApiClient<Long, PolicyTask> {

    @Autowired
    public PolicyTaskClient(RestTemplate restTemplate,
                            @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = PolicyTask.class;
        this.endpointUrl = apiEndpointUrl + "/policy-task";
    }


    public PolicyTask[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, PolicyTask[].class);
    }

    public PolicyTask[] getPage(Long policyJobId, int page, int limit) {
        String url = String.format("%s/get-page?policyJobId=%s&page=%d&limit=%d", this.endpointUrl, policyJobId, page, limit);
        return this.restTemplate.getForObject(url, PolicyTask[].class);
    }

    public Map<String, Long> getSummary(Long policyJobId) {
        String url = String.format("%s/get-summary?policyJobId=%s", this.endpointUrl, policyJobId);
        String json = this.restTemplate.getForObject(url, String.class);

        Map<String, Long> map = new LinkedHashMap<String, Long>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(json, new TypeReference<LinkedHashMap<String, String>>() { });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    public LoggingDeviceActivity[] getPageDeviceActivity(String deviceId, String fromDateTime, String toDateTime, int page, int limit) {
        String url = String.format("%s/get-page-device-activity?deviceId=%s&fromDateTime=%s&toDateTime=%s&page=%d&limit=%d", this.endpointUrl, deviceId, fromDateTime, toDateTime, page, limit);
        return this.restTemplate.getForObject(url, LoggingDeviceActivity[].class);
    }

    public Map<String, Long> getSummaryDeviceActivity(String deviceId, String fromDateTime, String toDateTime) {
        String url = String.format("%s/get-summary-device-activity?deviceId=%s&fromDateTime=%s&toDateTime=%s", this.endpointUrl, deviceId, fromDateTime, toDateTime);
        String json = this.restTemplate.getForObject(url, String.class);

        Map<String, Long> map = new LinkedHashMap<String, Long>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            map = mapper.readValue(json, new TypeReference<LinkedHashMap<String, Long>>() { });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    public long count(String query) {
        return this.restTemplate.postForObject(this.endpointUrl + "/count", query, long.class);
    }

    public PolicyTask findByTaskId(String taskId) {
        String url = String.format("%s/find-by-taskId?taskId=%s",
                this.endpointUrl, taskId);
        return this.restTemplate.getForObject(url, PolicyTask.class);
    }

    public Boolean deleteDeviceActivity(String id) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("id", id);
        String url = String.format("%s/delete-device-activity", this.endpointUrl);
        return this.restTemplate.postForObject(url, data, Boolean.class);
    }
}
