package vn.vnpt.ssdc.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.DeviceTypeVersionPaginator;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by vietnq on 11/2/16.
 */
@Component
public class DeviceTypeVersionClient extends GenericApiClient<Long, DeviceTypeVersion> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceTypeVersionClient.class);

    @Autowired
    public DeviceTypeVersionClient(RestTemplate restTemplate,
                                   @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = DeviceTypeVersion.class;
        this.endpointUrl = apiEndpointUrl + "/device-type-versions";
    }

    public Tag[] findTagsForDeviceTypeVersion(Long deviceTypeVersionId) {
        String url = String.format("%s/%d/tags", endpointUrl, deviceTypeVersionId);
        return this.restTemplate.getForObject(url, Tag[].class);
    }

    public Tag[] findAssignedTagsForDeviceTypeVersion(Long deviceTypeVersionId) {
        String url = String.format("%s/%d/assigned-tags", endpointUrl, deviceTypeVersionId);
        return this.restTemplate.getForObject(url, Tag[].class);
    }

    public DeviceTypeVersion[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, DeviceTypeVersion[].class);
    }

    /**
     * Returns previous version of a device
     *
     * @param deviceTypeVersionId
     * @return a device type version right before given device type version
     */
    public DeviceTypeVersion prev(Long deviceTypeVersionId) {
        String url = String.format("%s/%d/prev", endpointUrl, deviceTypeVersionId);
        try {
            return this.restTemplate.getForObject(url, DeviceTypeVersion.class);
        } catch (RestClientException e) {
            logger.error("{}", e);
            return null;
        }
    }

    public DeviceTypeVersion findByPk(Long deviceTypeId, String version) {
        String url = String.format("%s/find-by-pk?deviceTypeId=%d&version=%s",
                this.endpointUrl, deviceTypeId, version);
        return this.restTemplate.getForObject(url, DeviceTypeVersion.class);
    }

    public DeviceTypeVersion findByFirmwareVersion(String firmwareVersion) {
        String url = String.format("%s/find-by-firmware-version?firmwareVersion=%s", this.endpointUrl, firmwareVersion);
        return this.restTemplate.getForObject(url, DeviceTypeVersion.class);
    }

    public DeviceTypeVersion findByDevice(String id) {
        String url = String.format("%s/find-by-device?deviceId=%s",
                this.endpointUrl, id);
        return this.restTemplate.getForObject(url, DeviceTypeVersion.class);
    }

    public DeviceTypeVersion[] findInfoDevices(Map<String, String> queryParams, String deviceTypeId) {
        String url = String.format("%s/search-devices?limit=%s&indexPage=%s&deviceTypeId=%s",
                this.endpointUrl, queryParams.get("limit"), queryParams.get("indexPage"), deviceTypeId);
        return this.restTemplate.getForObject(url, DeviceTypeVersion[].class);
    }

    public DeviceTypeVersion[] getDeviceTypeIDForSortAndSearch( Map<String, String> dataModelQuery) {
        String url = String.format("%s/get-device-type-id-for-sort-and-search?" +
                        "sort=%s&limit=%s&indexPage=%s",
                this.endpointUrl, dataModelQuery.get("sort"), dataModelQuery.get("limit"),
                dataModelQuery.get("indexPage"));
        Map<String, String> requestParam = new HashMap<String, String>();
        requestParam.put("manufacturer",dataModelQuery.get("manufacturer"));
        requestParam.put("modelName",dataModelQuery.get("modelName"));
        return this.restTemplate.postForObject(url, requestParam,DeviceTypeVersion[].class);
    }

    public int countDeviceTypeIDForSortAndSearch( Map<String, String> dataModelQuery) {
        String url = String.format("%s/count-device-type-id-for-sort-and-search?" +
                        "sort=%s&limit=%s&indexPage=%s",
                this.endpointUrl, dataModelQuery.get("sort"), dataModelQuery.get("limit"),
                dataModelQuery.get("indexPage"));
        Map<String, String> requestParam = new HashMap<String, String>();
        requestParam.put("manufacturer",dataModelQuery.get("manufacturer"));
        requestParam.put("modelName",dataModelQuery.get("modelName"));
        return this.restTemplate.postForObject(url, requestParam, Integer.class);
    }
    public Map<String, Long> getWithDeviceTypeId() {
        String url = String.format("%s/find-all-device-type-id", this.endpointUrl);
        return this.restTemplate.getForObject(url, HashMap.class);
    }

    public DeviceTypeVersionPaginator getPage(int page, int limit) {
        return this.restTemplate.getForObject(String.format("%s/get-page?page=%d&limit=%d", this.endpointUrl, page, limit), DeviceTypeVersionPaginator.class);
    }

    public DeviceTypeVersion[] findByManufacturerAndModelName(String manufacturer, String modelName) {
        Map<String, String> requestParam = new HashMap<String, String>();
        requestParam.put("manufacturer",manufacturer);
        requestParam.put("modelName",modelName);
        return this.restTemplate.postForObject(String.format("%s/find-by-manufacturer-and-modelName",
                this.endpointUrl), requestParam, DeviceTypeVersion[].class);
    }

    public DeviceTypeVersion[] findByManufacturerAndModelNameAndFirm(String manufacturer, String modelName, String firmware) {
        Map<String, String> requestParam = new HashMap<String, String>();
        requestParam.put("manufacturer",manufacturer);
        requestParam.put("modelName",modelName);
        requestParam.put("firmware",firmware);
        return this.restTemplate.postForObject(String.format("%s/find-by-manu-and-model-and-firm",
                this.endpointUrl), requestParam, DeviceTypeVersion[].class);
    }

    public String pingDevices(String ipDevice) {
        String url = String.format("%s/pingDevice", this.endpointUrl);
        Map<String, String> data = new HashMap<>();
        data.put("ipDevice",ipDevice);
        return this.restTemplate.postForObject(url, data, String.class);
    }
}
