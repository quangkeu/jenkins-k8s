package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Permissions;

import java.util.Map;

/**
 * Created by Lamborgini on 5/5/2017.
 */
@Component
public class PermissionsClient extends GenericApiClient<Long, Permissions> {

    @Autowired
    public PermissionsClient(RestTemplate restTemplate,
                             @Value("${identityServiceEndpointUrl}") String identityServiceEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Permissions.class;
        this.endpointUrl = identityServiceEndpointUrl + "/permissions";
    }

    public Permissions[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, Permissions[].class);
    }

    public Permissions[] findPermissionPage(Map<String, String> requestParams) {
        String url = String.format("%s/search-permission?limit=%s&indexPage=%s",
                this.endpointUrl, requestParams.get("limit"), requestParams.get("indexPage"));
        return this.restTemplate.getForObject(url, Permissions[].class);
    }

    public int checkGroupName(String addGroupName, String addName) {
        String url = String.format("%s/check-group-name?addGroupName=%s&addName=%s",
                this.endpointUrl, addGroupName,addName);
        return this.restTemplate.getForObject(url, Integer.class);
    }
}
