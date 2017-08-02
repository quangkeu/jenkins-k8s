package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.vnpt.ssdc.api.client.ParameterDetailClient;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.api.model.Parameter;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

@Controller
public class DeviceSettingsController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(DeviceSettingsController.class);
    @Autowired
    protected ParameterDetailClient parameterDetailClient;

    private final String INDEX_URL = "/devices/device_settings";
    private final String INDEX_TEMPLATE = "devices/fragments/device_settings";


    @GetMapping(INDEX_URL + "/{deviceId}")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    public String index(Model model, @PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to setting page",deviceId,"");
        DeviceTypeVersion currentDeviceTypeVersion = deviceTypeVersionClient.findByDevice(deviceId);

        // Get device info
        Device currentDevice = null;
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


    @GetMapping(INDEX_URL + "/{deviceId}/get-list-tags")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    @ResponseBody
    public Map<String, Set<Object>> getListTags(@PathVariable("deviceId") String deviceId,
                                                @RequestParam(value = "deviceTypeVersionId", defaultValue = "0") Long deviceTypeVersionId,
                                                @RequestParam(value = "parentObject", defaultValue = "") String parentObjectSearch) {

        Map<String, Set<Object>> response = new HashMap<String, Set<Object>>();
        Set<Object> resultTags = new LinkedHashSet<>();
        Set<String> parentObjects = new TreeSet<>();
        Set<Object> resultParentObjects = new TreeSet<>();
        Tag firstTag = new Tag();

        // Get list tags
        Tag[] tags = tagClient.findSynchronizedByDeviceTypeVersion(deviceTypeVersionId);
        sortTags(tags);
        for (Tag tag: tags) {
            for (String path : tag.parameters.keySet()) {
                if(!"".equals(parentObjectSearch) && path.contains(parentObjectSearch)) {
                    tag.setResultSearch(true);
                }
                tag.parameters = new HashMap<>();
                resultTags.add(tag);
            }
        }

        response.put("listTags", resultTags);
        response.put("listParentObjects", resultParentObjects);

        return response;
    }

    @GetMapping(INDEX_URL + "/{deviceId}/get-list-parent-objects")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    @ResponseBody
    public Map<String, Set<String>> getListParentObject(@PathVariable("deviceId") String deviceId,
                                           @RequestParam(value = "tagId", defaultValue = "0") Long tagId,
                                           @RequestParam(value = "parentObject", defaultValue = "") String parentObjectSearch) {
        Set<String> parentObjects = new TreeSet<>();
        Set<String> parentObjectResultSearch = new TreeSet<>();
        Tag tag = tagClient.get(tagId);
        Parameter[] parameters = tagClient.getParametersOfDevice(deviceId, tag.id);
        for (Parameter parameter : parameters) {
            if (!ObjectUtils.empty(parameter) && !"object".equals(parameter.dataType)) {
                parentObjects.add(parameter.parentObject);

                if (!"".equals(parentObjectSearch) && parameter.path.contains(parentObjectSearch)) {
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
                                            @RequestParam(value = "tagId", defaultValue = "0") Long tagId,
                                            @RequestParam(value = "parentObject", defaultValue = "") String parentObject) {

        Set<Parameter> result = new LinkedHashSet<>();

        Tag tag = tagClient.get(tagId);
        if(tag != null) {
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

    private void sortTags(Tag[] tags) {
        Arrays.sort(tags, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

}
