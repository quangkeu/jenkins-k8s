package vn.vnpt.ssdc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by THANHLX on 6/8/2017.
 */
@EnableOAuth2Client
@Configuration
public class OAuthClientConfig {
    @Value("${oauth.resource:http://localhost:8090}")
    private String baseUrl;
    @Value("${oauth.authorize:http://localhost:8090/oauth/authorize}")
    private String authorizeUrl;
    @Value("${oauth.token:http://localhost:8090/oauth/token}")
    private String tokenUrl;


    @Bean
    protected OAuth2ProtectedResourceDetails resource() {

        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();

        List scopes = new ArrayList<String>(2);
        scopes.add("write");
        scopes.add("read");
        scopes.add("trust");
        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId("ump");
        resource.setClientSecret("ump@2016");
        resource.setGrantType("password");
        resource.setScope(scopes);

        resource.setUsername("ump");
        resource.setPassword("ump@2016");

        return resource;
    }

    @Bean
    public OAuth2RestTemplate restTemplate() {
        AccessTokenRequest atr = new DefaultAccessTokenRequest();
        return new OAuth2RestTemplate(resource(), new DefaultOAuth2ClientContext(atr));
    }
}
