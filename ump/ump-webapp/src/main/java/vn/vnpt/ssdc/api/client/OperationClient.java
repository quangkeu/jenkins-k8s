package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Operation;

/**
 * Created by Lamborgini on 5/9/2017.
 */
@Component
public class OperationClient extends GenericApiClient<String, Operation>{

    @Autowired
    public OperationClient(RestTemplate restTemplate,
                      @Value("${identityServiceEndpointUrl}") String identityServiceEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Operation.class;
        this.endpointUrl = identityServiceEndpointUrl + "/operation";
    }

    public Operation[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, Operation[].class);
    }
}
