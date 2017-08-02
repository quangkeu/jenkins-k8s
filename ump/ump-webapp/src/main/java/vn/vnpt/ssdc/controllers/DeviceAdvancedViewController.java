package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.vnpt.ssdc.api.client.LoggingUserClient;
import vn.vnpt.ssdc.api.client.ParameterDetailClient;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.api.model.Parameter;
import vn.vnpt.ssdc.api.model.ParameterDetail;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Controller
public class DeviceAdvancedViewController extends BaseController{
    private Logger logger = LoggerFactory.getLogger(DeviceAdvancedViewController.class);
    private final String INDEX_URL = "/devices/device_advanced_view";
    private static final String INDEX_TEMPLATE = "devices/fragments/device_advanced_view";

    @Autowired
    LoggingUserClient loggingUserClient;

    @Autowired
    protected ParameterDetailClient parameterDetailClient;

    @GetMapping(INDEX_URL + "/{deviceId}")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    public String index(Model model, @PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to list users page","","");
        Device currentDevice = null;
        DeviceTypeVersion currentDeviceTypeVersion = null;

        Map<String, String> indexParams = indexParamsDetail();
        Map<String, String> acsQuery = new HashMap<>();
        acsQuery.put("query", String.format("{\"_id\":\"%s\"}", deviceId));
        acsQuery.put("parameters", String.join(",", indexParams.keySet()));
        AcsResponse response = acsApiClient.findDevices(acsQuery);
        List<Device> devices = Device.fromJsonString(response.body, indexParams.keySet());
        if(devices.size() > 0) {
            currentDevice = devices.get(0);
            currentDeviceTypeVersion = deviceTypeVersionClient.findByDevice(deviceId);
        }

        // Add attribute
        model.addAttribute("currentDevice", currentDevice);
        model.addAttribute("currentDeviceTypeVersionId", currentDeviceTypeVersion != null ? currentDeviceTypeVersion.id : null);
        return INDEX_TEMPLATE;
    }

    @PostMapping(INDEX_URL + "/{deviceId}/update")
    @PreAuthorize("hasAuthority('API:ACS:SET-PARAMETER-VALUES')")
    public String update(@PathVariable("deviceId") String deviceId,
                         @RequestParam Map<String, String> params) {
        Map<String, Object> parameterMap = new HashMap<>();
        for (String key : params.keySet()) {
            if (key.startsWith("path_")) {
                String path = key.substring("path_".length(), key.length());
                String value = params.get(key);

                // Convert data if data type is date time
                String dataType = params.containsKey("dataType_" + path) ? params.get("dataType_" + path) : "";
                if("dateTime".equals(dataType)) {
                    value = StringUtils.toZoneDateTime(value);
                }

                parameterMap.put(path, value);
            }
        }

        // Update device
        AcsResponse resultTmp = acsApiClient.setParameterValues(deviceId, parameterMap);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"configuration",deviceId,loggingUserClient.getTaskId(resultTmp));
        String url = INDEX_URL + "/" + deviceId;
        if(params.containsKey("currentUrl")) {
            url = params.get("currentUrl");
        }
        return "redirect:" + url;
    }

    @PostMapping(INDEX_URL + "/{deviceId}/add-object")
    @PreAuthorize("hasAuthority('API:ACS:ADD-OBJECT')")
    public String addObjectParameter(@PathVariable("deviceId") String deviceId,
                                     @RequestParam("objectPath") String objectPath,
                                     @RequestParam Map<String, String> params) {

        Map<String, String> parameterMap = new HashMap<>();
        for (String key : params.keySet()) {
            if (key.startsWith("path_")) {
                String path = key.substring("path_".length(), key.length());
                String subPath = path.substring(path.lastIndexOf(".")+1, path.length());
                String value = params.get(key);

                // Convert data if data type is date time
                String dataType = params.containsKey("dataType_" + path) ? params.get("dataType_" + path) : "";
                if("dateTime".equals(dataType)) {
                    value = StringUtils.toZoneDateTime(value);
                }

                parameterMap.put(subPath, value);
            }
        }

        // Remove instants
        objectPath = objectPath.substring(0, objectPath.length()-1);

        // Update device
        AcsResponse resultTmp = acsApiClient.addObject(deviceId, true, objectPath, parameterMap);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"add object",deviceId,loggingUserClient.getTaskId(resultTmp));

        String url = INDEX_URL + "/" + deviceId;
        if(params.containsKey("currentUrlAddObject")) {
            url = params.get("currentUrlAddObject");
        }
        return "redirect:" + url;
    }

    @GetMapping(INDEX_URL + "/{deviceId}/get-list-parent-objects")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    @ResponseBody
    public Map<String, Set<String>> getListParentObject(@PathVariable("deviceId") String deviceId,
                                                        @RequestParam(value = "deviceTypeVersionId", defaultValue = "0") Long deviceTypeVersionId,
                                                        @RequestParam(value = "parentObject", defaultValue = "") String parentObjectSearch) {
        Set<String> parentObjects = new TreeSet<>();
        Set<String> parentObjectResultSearch = new TreeSet<>();
        Tag[] tags = tagClient.findSynchronizedByDeviceTypeVersion(deviceTypeVersionId);
        for (Tag tag : tags) {
            Parameter[] parameters = tagClient.getParametersOfDevice(deviceId, tag.id);
            for (Parameter parameter : parameters) {
                if (!ObjectUtils.empty(parameter) && !"object".equals(parameter.dataType)) {
                    parentObjects.add(parameter.parentObject);
                }

                if (!"".equals(parentObjectSearch) && !ObjectUtils.empty(parameter) && !"object".equals(parameter.dataType) && parameter.path.contains(parentObjectSearch)) {
                    parentObjectResultSearch.add(parameter.parentObject);
                }
            }
        }

        // Remove parent object non instance
        Set<String> resultParentObjects = new TreeSet<>(parentObjects);
        for (String parentObject : parentObjects) {
            String removeParentObject = "";
            for (String parentObject2 : parentObjects) {
                if(parentObject2.contains(parentObject) && !parentObject.equals(parentObject2)) {
                    removeParentObject = parentObject;
                }
            }
            resultParentObjects.remove(removeParentObject);
        }

        Map<String, Set<String>> result = new LinkedHashMap<>();
        result.put("parentObjects", resultParentObjects);
        result.put("parentObjectResultSearch", parentObjectResultSearch);

        return result;
    }

    @GetMapping(INDEX_URL + "/{deviceId}/get-list-parameters")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    @ResponseBody
    public Set<Parameter> getListParameters(@PathVariable("deviceId") String deviceId,
                                            @RequestParam(value = "deviceTypeVersionId", defaultValue = "0") Long deviceTypeVersionId,
                                            @RequestParam(value = "parentObject", defaultValue = "") String parentObject) {

        Set<Parameter> result = new LinkedHashSet<>();
        Tag[] tags = tagClient.findSynchronizedByDeviceTypeVersion(deviceTypeVersionId);
        for (Tag tag : tags) {
            Parameter[] parameters = tagClient.getParametersOfDevice(deviceId, tag.id);
            for (Parameter parameter : parameters) {
                if (parameter != null && !"object".equals(parameter.dataType) && parentObject.equals(parameter.parentObject)) {

                    if ("dateTime".equals(parameter.dataType)) {
                        String dateTime = ObjectUtils.empty(parameter.value) ? parameter.defaultValue : parameter.value;
                        dateTime = StringUtils.toSampleDate(dateTime);
                        parameter.value = dateTime;
                        parameter.defaultValue = dateTime;
                    }

                    result.add(parameter);
                }
            }
        }

        return result;
    }

    @GetMapping(INDEX_URL + "/get-status-button")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    @ResponseBody
    public Map<String, Object> getStatusButton(@RequestParam(value = "deviceTypeVersionId", defaultValue = "0") Long deviceTypeVersionId,
                                               @RequestParam(value = "parentObject", defaultValue = "") String parentObject) {

        Map<String, Object> response = new HashMap<>();
        Boolean isInstance = false;
        Boolean isDelete = false;
        ParameterDetail rootParameter = new ParameterDetail();

        // Convert to tr069 name
        parentObject = parentObject.replaceAll("\\.(\\d+)\\.", ".{i}.");
        ParameterDetail[] parameterDetails = parameterDetailClient.findByTr069Name(parentObject);

        if(!ObjectUtils.empty(parameterDetails) && parameterDetails.length > 0) {
            ParameterDetail currentParameter = parameterDetails[0];
            if (currentParameter != null) {
                if (currentParameter.instance != null && currentParameter.instance) {
                    isInstance = true;
                    String parentObject2 = currentParameter.tr069ParentObject;
                    ParameterDetail[] parameterDetails2 = parameterDetailClient.findByTr069Name(parentObject);
                    if(!ObjectUtils.empty(parameterDetails2) && parameterDetails2.length > 0) {
                        rootParameter = parameterDetails2[0];
                    }

                } else {
                    rootParameter = currentParameter;
                }
                isDelete = currentParameter.access != null && Boolean.valueOf(currentParameter.access) && isInstance;
            }
        }

        response.put("isAdd", Boolean.valueOf(rootParameter.access) && isInstance);
        response.put("isDelete", isDelete);

        return response;
    }

    @PostMapping(INDEX_URL + "/{deviceId}/post-refresh-object")
    @PreAuthorize("hasAuthority('API:ACS:REFRESH-OBJECT')")
    @ResponseBody
    public Boolean postRefreshObject(@PathVariable("deviceId") String deviceId,
                                     @RequestParam("parentObject") String objectStr) {
        AcsResponse resultTmp = acsApiClient.refreshObject(deviceId, objectStr);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"refresh object",deviceId,loggingUserClient.getTaskId(resultTmp));
        return true;
    }

    @PostMapping(INDEX_URL + "/{deviceId}/post-delete-object")
    @PreAuthorize("hasAuthority('API:ACS:DELETE-OBJECT')")
    @ResponseBody
    public Boolean postDeleteObject(@PathVariable("deviceId") String deviceId,
                                    @RequestParam("parentObject") String objectStr) {

        if(objectStr.lastIndexOf(".") == objectStr.length()-1) {
            objectStr = objectStr.substring(0, objectStr.length()-1);
        }
        AcsResponse resultTmp = acsApiClient.deleteObject(deviceId, true, objectStr);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete object",deviceId,loggingUserClient.getTaskId(resultTmp));
        return true;
    }

}
