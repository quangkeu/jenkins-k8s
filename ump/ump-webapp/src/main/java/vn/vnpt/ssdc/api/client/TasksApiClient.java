package vn.vnpt.ssdc.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SSDC on 11/11/2016.
 */
@Component
public class TasksApiClient extends GenericApiClient<Long, Task> {


    private static final Logger logger = LoggerFactory.getLogger(AcsApiClient.class);

    @Autowired
    public TasksApiClient(RestTemplate restTemplate,
                          @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Task.class;
        this.endpointUrl = apiEndpointUrl + "/tasks";
    }

    public AcsResponse findTaskByDeviceId(String deviceId) {
        String url = this.endpointUrl + "/" + deviceId;
        logger.info("Query: {}", url);
        return this.restTemplate.getForObject(url, AcsResponse.class);
    }

    public AcsResponse deleteTask(String taskId) {
        AcsResponse response = new AcsResponse();
        try {
            String url = this.endpointUrl + "/{taskId}";
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("taskId", taskId);
            logger.info("Query: {}", url);
            this.restTemplate.delete(url, uriVariables);
            response.httpResponseCode = 200;
        } catch (RestClientException ex) {
            response.httpResponseCode = 404;
            response.body = ex.getMessage();
            logger.error(ex.getMessage());
        }
        return response;

    }

    public AcsResponse retryTask(String taskId) {
        String url = this.endpointUrl + "/retry/{taskId}";
        logger.info("Query: {}", url);
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("taskId", taskId);
        Map<String, String> datas = new HashMap<>();
        ResponseEntity<String> result = restTemplate.postForEntity(url, datas, String.class, uriVariables);
        AcsResponse response = new AcsResponse();
        response.httpResponseCode = result.getStatusCodeValue();
        return response;
    }

    public AcsResponse findTaskNewest(String deviceId) {
        String url = this.endpointUrl + "/" + deviceId;
        logger.info("Query: {}", url);
        return this.restTemplate.getForObject(url, AcsResponse.class);
    }


}
