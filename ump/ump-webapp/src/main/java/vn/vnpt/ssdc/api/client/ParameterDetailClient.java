package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.ParameterDetail;

import java.util.HashMap;
import java.util.Map;

@Component
public class ParameterDetailClient extends GenericApiClient<Long, ParameterDetail> {

    private static final String PARAMETER_DETAIL_ENDPOINT = "/parameter-detail";

    @Autowired
    public ParameterDetailClient(RestTemplate restTemplate,
                                 @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.endpointUrl = apiEndpointUrl + PARAMETER_DETAIL_ENDPOINT;
        this.entityClass = ParameterDetail.class;
    }

    public ParameterDetail[] findByDeviceTypeVersion(Long deviceTypeVersionId) {
        String url = String.format("%s/find-by-device-type-version/%s", this.endpointUrl, deviceTypeVersionId);
        return this.restTemplate.getForObject(url, ParameterDetail[].class);
    }

    public ParameterDetail findByParams(String parentObject, Long deviceTypeVersionId) {
        String url = String.format("%s/find-by-params?path=%s&deviceTypeVersionId=%s", this.endpointUrl, parentObject, deviceTypeVersionId);
        return this.restTemplate.getForObject(url, ParameterDetail.class);
    }

    public ParameterDetail[] findAll() {
        return this.restTemplate.getForObject(this.endpointUrl, ParameterDetail[].class);
    }

    public ParameterDetail[] findParameters() {
        String url = String.format("%s/find-parameters", this.endpointUrl);
        return this.restTemplate.getForObject(url, ParameterDetail[].class);
    }

    public ParameterDetail[] findByTr069Name(String tr069Name) {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("tr069Name", tr069Name);
        String url = String.format("%s/find-by-tr069-name?tr069Name={tr069Name}", this.endpointUrl);
        return this.restTemplate.getForObject(url, ParameterDetail[].class, queryParams);
    }
}
