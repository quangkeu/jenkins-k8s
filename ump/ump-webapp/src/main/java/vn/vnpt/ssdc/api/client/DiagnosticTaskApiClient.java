package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.DiagnosticTask;

/**
 * Created by thangnc on 10-Mar-17.
 */
@Component
public class DiagnosticTaskApiClient extends GenericApiClient<Long, DiagnosticTask> {

    @Autowired
    public DiagnosticTaskApiClient(RestTemplate restTemplate,
                             @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = DiagnosticTask.class;
        this.endpointUrl = apiEndpointUrl + "/diagnostic";
    }

}
