package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.User;
import vn.vnpt.ssdc.models.UserPaginator;

import java.util.LinkedHashMap;
import java.util.Map;


@Component
public class UserClient extends GenericApiClient<Long, User> {
    @Autowired
    public UserClient(RestTemplate restTemplate,
                      @Value("${identityServiceEndpointUrl}") String identityServiceEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = User.class;
        this.endpointUrl = identityServiceEndpointUrl + "/user";
    }

    public User[] findAll() {
        return this.restTemplate.getForObject(String.format("%s/", this.endpointUrl), User[].class);
    }

    public UserPaginator getPage(int page, int limit, String whereStr) {
        return this.restTemplate.getForObject(String.format("%s/get-page?page=%d&limit=%d&where=%s", endpointUrl, page, limit, whereStr), UserPaginator.class);
    }

    public User getByUsername(String username) {
        return this.restTemplate.getForObject(String.format("%s/get-by-username/%s", this.endpointUrl, username), User.class);
    }

    public User postResetPassword(Long id) {
        return this.restTemplate.postForObject(String.format("%s/%d/reset-password", this.endpointUrl, id), null, User.class);
    }

    public Boolean forgotPassword(String username) {
        String url = String.format("%s/forgot-password", this.endpointUrl);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("username", username);

        return this.restTemplate.postForObject(url, data, Boolean.class);
    }

    public Boolean forgotPasswordWithEmail(String email) {
        return this.restTemplate.postForObject(String.format("%s/%s/forgot-password-with-email", this.endpointUrl, email), null, Boolean.class);
    }

    public Boolean checkCurrentPassword(String username, String currentPassword) {
        String url = String.format("%s/check-current-password", this.endpointUrl);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("currentPassword", currentPassword);

        return this.restTemplate.postForObject(url, data, Boolean.class);
    }

    public User changePassword(String username, String currentPassword, String newPassword) {
        String url = String.format("%s/change-password", this.endpointUrl);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("newPassword", newPassword);
        data.put("currentPassword", currentPassword);

        return this.restTemplate.postForObject(url, data, User.class);
    }

    public Boolean changePasswordWithToken(Long userId, String token, String newPassword) {
        String url = String.format("%s/change-password-with-token", this.endpointUrl);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("userId", String.valueOf(userId));
        data.put("token", token);
        data.put("newPassword", newPassword);

        return this.restTemplate.postForObject(url, data, Boolean.class);
    }

    public User[] checkByRoleId(String paramName) {
        return this.restTemplate.getForObject(String.format("%s/check-by-role-id/%s", this.endpointUrl, paramName), User[].class);
    }

    public User[] findUserByGroupID(String groupId) {
        return this.restTemplate.getForObject(String.format("%s/get-user-by-group?groupId=%s",
                this.endpointUrl, groupId), User[].class);
    }
}