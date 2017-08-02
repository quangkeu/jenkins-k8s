package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.models.BasePaginator;

import java.util.Map;

/**
 * Created by Lamborgini on 6/13/2017.
 */
@Component
public class BackupFileClient {

    private RestTemplate restTemplate;
    private String apiEndpointUrl;

    @Autowired
    public BackupFileClient(@Value("${apiEndpointUrl}") String apiEndpointUrl,
                                RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiEndpointUrl = apiEndpointUrl + "/files";
    }

    public AcsResponse findFileManagement(Map<String, String> queryParams) {
        String url = String.format("%s/search-file/%s/%s/%s/%s", apiEndpointUrl,
                queryParams.get(BasePaginator.PAGE_QUERY), queryParams.get(BasePaginator.PAGE_SIZE),
                queryParams.get(BasePaginator.PAGE_OFFSET),queryParams.get(BasePaginator.PAGE_PARAM));
        return this.restTemplate.postForObject(url, queryParams,AcsResponse.class);
    }
}
