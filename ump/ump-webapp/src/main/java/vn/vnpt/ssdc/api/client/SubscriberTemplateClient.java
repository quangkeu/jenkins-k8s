package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.SubscriberTemplate;
import vn.vnpt.ssdc.models.SubscriberTemplatePaginator;

@Component
public class SubscriberTemplateClient extends GenericApiClient<Long, SubscriberTemplate> {

    private static final String SUBSCRIBER_TEMPLATE_ENDPOINT = "/subscriber-templates";

    @Autowired
    public SubscriberTemplateClient(RestTemplate restTemplate,
                                    @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.endpointUrl = apiEndpointUrl + SUBSCRIBER_TEMPLATE_ENDPOINT;
        this.entityClass = SubscriberTemplate.class;
    }

    public SubscriberTemplate[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, SubscriberTemplate[].class);
    }

    public SubscriberTemplatePaginator getPage(int page, int limit) {
        return this.restTemplate.getForObject(String.format("%s/get-page?page=%d&limit=%d", endpointUrl, page, limit), SubscriberTemplatePaginator.class);
    }
}