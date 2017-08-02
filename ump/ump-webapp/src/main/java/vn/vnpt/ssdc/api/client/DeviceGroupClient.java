package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.DeviceGroup;
import vn.vnpt.ssdc.models.Device;


import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by thangnc on 06-Feb-17.
 */
@Component
public class DeviceGroupClient extends GenericApiClient<Long, DeviceGroup>{

    @Autowired
    private HttpSession session;

    @Autowired
    public DeviceGroupClient(RestTemplate restTemplate,
                           @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = DeviceGroup.class;
        this.endpointUrl = apiEndpointUrl + "/device-group";
    }

    public DeviceGroup[] findAll() {
        String rolesStr = session.getAttribute("deviceGroupIds").toString();
        rolesStr = rolesStr.replaceAll("[\"\\[\\]]", "");

        String url = String.format("%s/find-all?deviceGroupIds=%s", endpointUrl, rolesStr);
        return this.restTemplate.getForObject(url, DeviceGroup[].class);
    }

    public DeviceGroup[] findByName(String name) {
        return this.restTemplate.getForObject(String.format("%s/find-by-name?name=%s",
                this.endpointUrl, name), DeviceGroup[].class);
    }

    public DeviceGroup[] findByPage(int offset, int limit) {
        String rolesStr = session.getAttribute("deviceGroupIds").toString();
        rolesStr = rolesStr.replaceAll("[\"\\[\\]]", "");

        String url = String.format("%s/find-by-page?offset=%d&limit=%d&deviceGroupIds=%s", this.endpointUrl, offset, limit, rolesStr);
        return this.restTemplate.getForObject(url, DeviceGroup[].class);
    }

    public Device[] getListDeviceByGroup(String deviceGroupId) {
        return this.restTemplate.getForObject(String.format("%s/find-by-group-id?deviceGroupId=%s",
                this.endpointUrl, deviceGroupId), Device[].class);
    }

    public String buildMongoQuery(String manufacturer, String modelName, String firmwareVersion, String label) {
        return this.restTemplate.getForObject(String.format("%s/build-mongo-query?manufacturer=%s&modelName=%s&firmwareVersion=%s&label=%s",
                this.endpointUrl, manufacturer, modelName, firmwareVersion, label), String.class);
    }
}
