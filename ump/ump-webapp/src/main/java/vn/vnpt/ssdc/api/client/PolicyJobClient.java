package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.PolicyJob;
import vn.vnpt.ssdc.models.PolicyJobPaginator;

import java.util.HashMap;

@Component
public class PolicyJobClient extends GenericApiClient<Long, PolicyJob> {

    @Autowired
    public PolicyJobClient(RestTemplate restTemplate, @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = PolicyJob.class;
        this.endpointUrl = apiEndpointUrl + "/policy";
    }

    public PolicyJob[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, PolicyJob[].class);
    }

    public PolicyJobPaginator getPage(int page, int limit) {
        String url = String.format("%s/get-page?page=%d&limit=%d", this.endpointUrl, page, limit);
        return this.restTemplate.getForObject(url, PolicyJobPaginator.class);
    }

    public PolicyJobPaginator getPageWithNumberOfExecution(int page, int limit) {
        String url = String.format("%s/get-page-with-number-of-execution?page=%d&limit=%d", this.endpointUrl, page, limit);
        return this.restTemplate.getForObject(url, PolicyJobPaginator.class);
    }

    public String execute(Long id) {
        String url = String.format("%s/%s/excute", this.endpointUrl, id);
        return this.restTemplate.postForObject(url, new HashMap<>(),String.class);
    }

    public String stop(Long id) {
        String url = String.format("%s/%s/stop", this.endpointUrl, id);
        return this.restTemplate.postForObject(url, new HashMap<>(),String.class);
    }

    public Boolean findJobExecute(String groupId, String status) {
        return this.restTemplate.getForObject(String.format("%s/check-job-by-group?groupId=%s&status=%s",
                this.endpointUrl, groupId, status), Boolean.class);
    }
}
