package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.Tag;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by vietnq on 11/2/16.
 */
@Component
public class DeviceTypeClient extends GenericApiClient<Long,DeviceType> {
    @Autowired
    public DeviceTypeClient(RestTemplate restTemplate,
                            @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = DeviceType.class;
        this.endpointUrl = apiEndpointUrl + "device-types";
    }

    public Tag[] findTagsForDeviceType(Long deviceTypeId) {
        String url = String.format("%s/%d/tags",endpointUrl,deviceTypeId);
        return this.restTemplate.getForObject(url,Tag[].class);
    }

    public DeviceType[] findAll() {
        return this.restTemplate.getForObject(endpointUrl,DeviceType[].class);
    }

    public Tag[] assignTags(Long deviceTypeId, List<Long> tags) {
        String url = String.format("%s/%d/tags",endpointUrl,deviceTypeId);
        return this.restTemplate.postForObject(url,tags,Tag[].class);
    }

    /**
     * Returns previous version of a device
     * @param deviceTypeID
     * @return a device type with version right before given device type
     */
    public DeviceType prev(Long deviceTypeID) {
        String url = String.format("%s/%d/prev",endpointUrl,deviceTypeID);
        try {
            return this.restTemplate.getForObject(url,DeviceType.class);
        } catch(RestClientException e) {
            return null;
        }
    }
}
