package vn.vnpt.ssdc.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.*;

import java.util.*;

/**
 * Created by SSDC on 11/14/2016.
 */

@Component
public class DiagnosticApiClient extends GenericApiClient<String, Diagnostic> {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticApiClient.class);

    @Autowired
    public DiagnosticApiClient(RestTemplate restTemplate,
                               @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Diagnostic.class;
        this.endpointUrl = apiEndpointUrl + "/diagnostic";
    }

    /**
     * Api for get avaiable list interface in device
     *
     * @param deviceId
     * @return List inteface
     */
    public List<String> getListInterface(String deviceId) {
        String url = this.endpointUrl + "/getInterfaceList";
        url += "?deviceId=" + deviceId;
        logger.info("URL get list interface {}", url);
        List<String> listInterfaces = null;
        Map<String, String> mapVariable = new HashMap<>();
        AcsResponse response = this.restTemplate.getForObject(url, AcsResponse.class, mapVariable);
        if (response.body != null) {
            String tmp = response.body;
            tmp = tmp.replace("[", "").replace("]", "");
            listInterfaces = new ArrayList<>(Arrays.asList(tmp.split(",")));

        }
        logger.info("ListInterface Avaiable in device {} : {}", deviceId, listInterfaces != null ? listInterfaces.toString() : "");
        return listInterfaces;
    }

    public DiagnosticTask[] getAllTask(String deviceId, int offset, int limit, String mode) {
        String url = String.format("%s/%s/getAllTask", this.endpointUrl, deviceId);
        url += "?offset=" + offset + "&limit=" + limit + "&mode=" + mode;
        return this.restTemplate.getForObject(url, DiagnosticTask[].class);
    }

    public List<Map<String, Tag>> getListDiagnostics(String manufacturer, String oui, String productClass, String fwVersion) {
        String url = String.format("%s/%s/%s/%s/%s/listDiagnostics", this.endpointUrl, manufacturer, oui , productClass, fwVersion);
        String json = restTemplate.getForObject(url, String.class);
        List<Map<String, Tag>> map = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map =  mapper.readValue(json, new TypeReference<List<Map<String, Tag>>>(){});
        } catch (Exception e) {
            logger.info("Exception converting {} to map", json, e);
        }
        return map;
    }

    public long insertDiagnosticsModel(String deviceId, String diagnosticName, Map<String, String> requestParams) {
        requestParams.remove("type");
        String url = String.format("%s/%s/%s/createNew", this.endpointUrl, deviceId, diagnosticName);
        return this.restTemplate.postForObject(url, requestParams, Long.class);
    }
}