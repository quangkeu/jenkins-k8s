package vn.ssdc.vnpt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by vietnq on 10/21/16.
 */
@Component
public class AcsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcsClient.class);

    private static final int POST = 0x01;

    private static final int DELETE = 0x02;

    private static final int PUT = 0x03;

    private RestTemplate restTemplate;
    private String acsEndpoint;

    @Autowired
    public AcsClient(@Value("${acsEndpoint}") String acsEndpoint) {
        this.acsEndpoint = acsEndpoint;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Search for items in database (e.g. devices, tasks, presets, files)
     *
     * @param collectionName: The data collection to search. Could be one of: tasks, devices, presets, objects.
     * @param queryParams:    Search query. Refer to MongoDB queries for reference.
     * @return Returns a JSON representation of all items in the given collection that match the search criteria.
     */
    public ResponseEntity<String> search(String collectionName, Map<String, String> queryParams) {
        String url = this.acsEndpoint + "/" + collectionName;
        String queryString = StringUtils.queryStringFromMap(queryParams);
        if (!ObjectUtils.empty(queryString)) {
            url += "?" + queryString;
        }
        return this.restTemplate.getForEntity(url, String.class, queryParams);
    }

    /**
     * Reboot a specific device.
     *
     * @param deviceId    The ID of the device
     * @param queryParams params of header
     * @return ResponseEntity represent for returned result
     */
    public ResponseEntity<String> reboot(String deviceId, Map<String, String> queryParams) throws RestClientException {
        Map<String, String> datas = new HashMap<String, String>();
        datas.put("name", "reboot");
        return postTasksToDevice(deviceId, datas, queryParams);
    }

    /**
     * Factory reset a specific device.
     *
     * @param deviceId    The ID of the device
     * @param queryParams params of header
     * @return ResponseEntity represent for returned result
     */
    public ResponseEntity<String> factoryReset(String deviceId, Map<String, String> queryParams) throws RestClientException {
        Map<String, String> datas = new HashMap<String, String>();
        datas.put("name", "factoryReset");
        return postTasksToDevice(deviceId, datas, queryParams);
    }

    /**
     * Append a task to queue for a given device.
     *
     * @param deviceId    The ID of the device
     * @param datas       Collection of data to send
     * @param queryParams params of header
     * @return ResponseEntity represent for returned result
     */
    private ResponseEntity<String> postTasksToDevice(String deviceId, Map<String, String> datas, Map<String, String> queryParams) throws RestClientException {
        StringBuilder url = new StringBuilder("/devices/" + deviceId + "/tasks");
        String queryString = StringUtils.queryStringFromMap(queryParams);
        if (!ObjectUtils.empty(queryString)) {
            url.append("?" + queryString);
        }
        return restTemplate.postForEntity(acsEndpoint + url.toString(), datas, String.class, queryParams);
    }

    /**
     * Retry a faulty task at the next inform.
     *
     * @param taskId : The ID of the task as returned by 'GET /tasks' request
     * @return Response entity
     */
    public ResponseEntity<String> retryTask(String taskId) {
        String fullPath = acsEndpoint + "/tasks/{taskId}/retry";
        Map<String, String> uriVariables = new HashMap<String, String>();
        uriVariables.put("taskId", taskId);
        Map<String, String> datas = new HashMap<String, String>();
        ResponseEntity<String> result = restTemplate.postForEntity(fullPath, datas, String.class, uriVariables);
        return result;
    }


    /**
     * Delete specific taskId
     *
     * @param taskId
     * @throws RestClientException if delete is not success
     */
    public void deleteTask(String taskId) throws RestClientException {
        String fullPath = acsEndpoint + "/tasks/{taskId}";
        Map<String, String> uriVariables = new HashMap<String, String>();
        uriVariables.put("taskId", taskId);
        restTemplate.delete(fullPath, uriVariables);
    }



    /*public ResponseEntity<String> manipulateTagToDevice(String deviceId, String tag, int method) {
        String path = "/devices/{device_id}/tags/{tag}";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("device_id", deviceId);
        map.add("tag", tag);
        if (method == POST) {
            return restTemplate.postForEntity(acsEndpoint + path, null, String.class,map);
        } else if (method == DELETE) {
            restTemplate.delete(acsEndpoint + path, map);
            return "Delete success";

        } else {
            return "Method is invalid";
        }
    }*/
//
//    //Preset
//    public String manipulatePreset(String presetName, String deviceId, List<String> keys, List<String> values, int method) {
//        String fullpath = acsEndpoint + "/presets/{presetName}";
//        if (method == PUT) {
//            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
//            if (keys == null || keys.size() == 0 || values == null || values.size() == 0) {
//                throw new AcsClientException("The parameter is invalid");
//            }
//            for (int i = 0; i < keys.size(); i++) {
//                map.add(keys.get(i), values.get(i));
//            }
//            Map<String, String> params = new HashMap<String, String>();
//            params.put("presetName", presetName);
//            restTemplate.put(fullpath, map, params);
//            return "put success";
//        } else if (method == DELETE) {
//            restTemplate.delete(acsEndpoint + fullpath);
//            return "Delete success";
//        } else {
//            return "The method is invalid";
//        }
//
//    }
//
//    //File
//
//
//    public String convertValueToQuery(String key, String value) {
//        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
//            return null;
//        }
//        String queryString = "{\"_id\":\"value\"}";
//        queryString = queryString.replace("_id", key);
//        queryString = queryString.replace("value", value);
//        return queryString;
//    }
}
