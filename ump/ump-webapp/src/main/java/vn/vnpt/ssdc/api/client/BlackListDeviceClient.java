package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.BlacklistDevice;

/**
 * Created by Lamborgini on 3/3/2017.
 */
@Component
public class BlackListDeviceClient extends GenericApiClient<Long, BlacklistDevice> {

    @Autowired
    public BlackListDeviceClient(RestTemplate restTemplate,
                                 @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = BlacklistDevice.class;
        this.endpointUrl = apiEndpointUrl + "/blacklist-devices";
    }
}
