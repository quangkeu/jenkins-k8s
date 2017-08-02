package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.ExceptionResponse;
import vn.vnpt.ssdc.api.model.SubscriberDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Huy Hieu on 11/25/2016.
 */

@Component
public class SubscriberDeviceClient extends GenericApiClient<Long, SubscriberDevice> {

    private static final String SUBSCRIBER_DEVICE_ENDPOINT = "/subscriber-devices";

    @Autowired
    public SubscriberDeviceClient(RestTemplate restTemplate,
                                  @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.endpointUrl = apiEndpointUrl + SUBSCRIBER_DEVICE_ENDPOINT;
        this.entityClass = SubscriberDevice.class;
    }


    public SubscriberDevice[] getSubscriberDeviceBySubscriberId(String subscriberId) {
        String url = String.format("%s/find-by-subscriber-id?subscriberId=%s", this.endpointUrl, subscriberId);
        SubscriberDevice[] subscriberDevices = restTemplate.getForObject(url, SubscriberDevice[].class);
        return subscriberDevices;
    }

    public SubscriberDevice[] findAll() {
        String url = String.format(this.endpointUrl);
        SubscriberDevice[] subscriberDevices = restTemplate.getForObject(url, SubscriberDevice[].class);
        return subscriberDevices;
    }


    public boolean replaceCPE(String oldDeviceId, String newDeviceId) {
        String url = String.format("%s/replace-cpe/%s/%s", this.endpointUrl, oldDeviceId,newDeviceId);
        return this.restTemplate.getForObject(url, Boolean.class);
    }

    public boolean countSubscriberDeviceByDeviceId(String deviceId) {
        String url = String.format("%s/find-by-device-id?deviceId=%s", this.endpointUrl, deviceId);
        return restTemplate.getForObject(url, Boolean.class);
    }
}
