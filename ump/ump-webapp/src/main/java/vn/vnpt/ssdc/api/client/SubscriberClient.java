package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Subscriber;
import vn.vnpt.ssdc.api.model.SubscriberDevice;
import vn.vnpt.ssdc.models.SubscriberPaginator;

/**
 * Created by Huy Hieu on 11/24/2016.
 */
@Component
public class SubscriberClient extends GenericApiClient<Long, Subscriber> {

    private static final String SUBSCRIBER_ENDPOINT = "/subscribers";

    @Autowired
    public SubscriberClient(RestTemplate restTemplate,
                            @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.endpointUrl = apiEndpointUrl + SUBSCRIBER_ENDPOINT;
        this.entityClass = Subscriber.class;
    }

    public Subscriber findBySubscriberId(String subscriberId) {
        String url = String.format("%s/find-by-subscriber-id?subscriberId=%s", this.endpointUrl, subscriberId);
        return this.restTemplate.getForObject(url, Subscriber.class);
    }

    public Subscriber[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, Subscriber[].class);
    }

    public boolean provisioningSubcriber(String subscriberId, SubscriberDevice deviceBeProvision) {
        String url = String.format("%s/provisiong-subcriber?subscriberId=%s", this.endpointUrl, subscriberId);
        return this.restTemplate.postForObject(url, deviceBeProvision, Boolean.class);
    }

    public SubscriberPaginator getPage(int page, int limit) {
        return this.restTemplate.getForObject(String.format("%s/get-page?page=%d&limit=%d", endpointUrl, page, limit), SubscriberPaginator.class);
    }

    public Subscriber getBySubscriberTemplateId(Long subscriberTemplateId) {
        String url = String.format("%s/get-by-subscriber-template-id?subscriberTemplateId=%d", endpointUrl, subscriberTemplateId);
        return this.restTemplate.getForObject(url, Subscriber.class);
    }


}
