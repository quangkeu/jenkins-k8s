package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.utils.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vietnq on 11/2/16.
 */
@Component
public class DeviceTypeClient extends GenericApiClient<Long, DeviceType> {
    @Autowired
    public DeviceTypeClient(RestTemplate restTemplate,
                            @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = DeviceType.class;
        this.endpointUrl = apiEndpointUrl + "/device-types";
    }

    public DeviceType[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, DeviceType[].class);
    }

    public DeviceTypeVersion[] findVersions(Long deviceTypeId) {
        String url = String.format("%s/%d/versions", this.endpointUrl, deviceTypeId);
        return this.restTemplate.getForObject(url, DeviceTypeVersion[].class);
    }

    public DeviceType findByPk(String manufacturer, String oui, String productClass) {
        String url = String.format("%s/find-by-pk?manufacturer=%s&oui=%s&productClass=%s",
                this.endpointUrl, manufacturer, oui, productClass);
        return this.restTemplate.getForObject(url, DeviceType.class);
    }

    public Boolean isExisted(Long id, String name, String manufacturer, String oui, String productClass) {
        String url = String.format("%s/is-existed?id=%s&name=%s&manufacturer=%s&oui=%s&productClass=%s",
                this.endpointUrl, id, name, manufacturer, oui, productClass);
        return this.restTemplate.getForObject(url, Boolean.class);
    }

    public DeviceType[] findByManufacturerAndModelName(String manufacturer, String modelName) {
        String urlPostObject = this.endpointUrl + "/find-by-manufacturer-and-modelName";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put("manufacturer", manufacturer);
        bodyVariable.put("modelName", modelName);
        return this.restTemplate.postForObject(urlPostObject, bodyVariable, DeviceType[].class, new HashMap<String, String>());

    }
}
