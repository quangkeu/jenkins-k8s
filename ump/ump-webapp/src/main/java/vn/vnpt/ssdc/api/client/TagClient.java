package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.Tag;

/**
 * Created by vietnq on 11/2/16.
 */
@Component
public class TagClient extends GenericApiClient<Long,Tag> {
    @Autowired
    public TagClient(RestTemplate restTemplate,
                     @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Tag.class;
        this.endpointUrl = apiEndpointUrl + "tags";
    }
}
