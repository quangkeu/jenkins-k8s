package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Role;

import java.util.Map;

/**
 * Created by Lamborgini on 5/4/2017.
 */
@Component
public class RoleClient extends GenericApiClient<Long, Role>{

    @Autowired
    public RoleClient(RestTemplate restTemplate,
                      @Value("${identityServiceEndpointUrl}") String identityServiceEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Role.class;
        this.endpointUrl = identityServiceEndpointUrl + "/role";
    }

    public Role[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, Role[].class);
    }

    public Role[] findRole(Map<String, String> rolesQueryParam) {
        String url = String.format("%s/search-role?limit=%s&indexPage=%s",
                this.endpointUrl, rolesQueryParam.get("limit"), rolesQueryParam.get("indexPage"));
        return this.restTemplate.getForObject(url, Role[].class);
    }

    public int checkRoleName(String addName) {
        String url = String.format("%s/check-role-name?addNameRole=%s",
                this.endpointUrl, addName);
        return this.restTemplate.getForObject(url, Integer.class);
    }

    public Role[] checkByPermissionId(String permissionId) {
        return this.restTemplate.getForObject(String.format("%s/check-by-permission-id/%s", this.endpointUrl, permissionId), Role[].class);
    }

    public Role[] getChildrenRoles(String currentUsername) {
        String url = String.format("%s/get-children-roles?username=%s", this.endpointUrl, currentUsername);
        return this.restTemplate.getForObject(url, Role[].class);
    }
}
