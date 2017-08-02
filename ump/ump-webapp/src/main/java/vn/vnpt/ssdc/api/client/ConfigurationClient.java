package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Configuration;

@Component
public class ConfigurationClient extends GenericApiClient<String, Configuration>{

    @Autowired
    public ConfigurationClient(RestTemplate restTemplate,
                               @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Configuration.class;
        this.endpointUrl = apiEndpointUrl + "/configuration";
    }

}
