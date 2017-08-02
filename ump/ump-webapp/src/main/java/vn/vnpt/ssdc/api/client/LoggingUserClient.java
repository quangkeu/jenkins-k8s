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
import vn.vnpt.ssdc.api.model.LoggingUser;
import vn.vnpt.ssdc.models.LoggingUserPaginator;

@Component
public class LoggingUserClient extends GenericApiClient<Long, LoggingUser> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingUserClient.class);

    private static final String LOGGING_DEVICE_ENDPOINT = "/logging/user";

    @Autowired
    public LoggingUserClient(RestTemplate restTemplate, @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.endpointUrl = apiEndpointUrl + LOGGING_DEVICE_ENDPOINT;
        this.entityClass = LoggingUser.class;
    }

    public LoggingUserPaginator getPage(int page, int limit,
                                   String name, String actor,
                                   String fromDateTime, String toDateTime) {
        String url = String.format("%s/get-page" +
                        "?page=%d&limit=%d" +
                        "&name=%s&actor=%s" +
                        "&fromDateTime=%s&toDateTime=%s",
                endpointUrl, page, limit, name, actor, fromDateTime, toDateTime);
        return this.restTemplate.getForObject(url, LoggingUserPaginator.class);
    }

    public Boolean removeAll(String name, String actor, String fromDateTime, String toDateTime) {
        String url = String.format("%s/remove-all?name=%s&actor=%s&fromDateTime=%s&toDateTime=%s", endpointUrl, name, actor, fromDateTime, toDateTime);
        return this.restTemplate.postForObject(url, null, Boolean.class);
    }

    public String getTaskId(AcsResponse acsResponse){
        JsonObject jsonObject = new Gson().fromJson(acsResponse.body, JsonObject.class);
        String taskId = jsonObject.get("_id").getAsString();
        return taskId;
    }
}
