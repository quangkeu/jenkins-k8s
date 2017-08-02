package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.models.File;
import vn.vnpt.ssdc.models.BasePaginator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lamborgini on 3/30/2017.
 */
@Component
public class FileManagementClient {

    private RestTemplate restTemplate;
    private String apiEndpointUrl;

    @Autowired
    public FileManagementClient(@Value("${apiEndpointUrl}") String apiEndpointUrl,
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

    public String deleteFile(String paramId, String paramName) {
        String url = String.format("%s/delete-file/%s/%s", apiEndpointUrl, paramId,paramName);
        Map<String, String> data = new HashMap<>();
        return this.restTemplate.postForObject(url, data, String.class);
    }

    public Boolean checkByVersion(List<File> fileList) {
        String urlGetObject = this.apiEndpointUrl + "/check-by-version";
        Map<String, Object> bodyVariable = new HashMap<>();
        bodyVariable.put("list", fileList);
        return this.restTemplate.postForObject(urlGetObject, bodyVariable, Boolean.class, new HashMap<String, String>());
    }

//    public String deleteFile(String paramId, String paramName) {
//        String url = String.format("%s/delete-file/%s/%s", apiEndpointUrl, paramId,paramName);
//        Map<String, String> data = new HashMap<>();
//        return this.restTemplate.postForObject(url, data, String.class);
//    }
}
