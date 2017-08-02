package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.EmailTemplat;

import java.util.Map;

/**
 * Created by Lamborgini on 6/7/2017.
 */
@Component
public class EmailTemplateClient extends GenericApiClient<String, EmailTemplat>{

    @Autowired
    public EmailTemplateClient(RestTemplate restTemplate,
                       @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = EmailTemplat.class;
        this.endpointUrl = apiEndpointUrl + "/email-template";
    }

    public EmailTemplat[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, EmailTemplat[].class);
    }

    public EmailTemplat[] findEmail(Map<String, String> emailQueryParam) {
        String url = String.format("%s/search-email?limit=%s&indexPage=%s",
                this.endpointUrl, emailQueryParam.get("limit"), emailQueryParam.get("indexPage"));
        return this.restTemplate.getForObject(url, EmailTemplat[].class);
    }
}
