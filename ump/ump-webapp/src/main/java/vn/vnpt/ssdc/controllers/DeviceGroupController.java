package vn.vnpt.ssdc.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.*;
import vn.vnpt.ssdc.api.model.*;
import vn.vnpt.ssdc.models.DeviceGroupPaginator;
import vn.vnpt.ssdc.models.DevicePaginator;
import vn.vnpt.ssdc.utils.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by thangnc on 06-Feb-17.
 */
@Controller
public class DeviceGroupController extends BaseController {

    private static final String GROUP_DEVICE_PAGE = "devices/device_group";
    private static final Logger logger = LoggerFactory.getLogger(DeviceGroupController.class);

    @Autowired
    private DeviceGroupClient deviceGroupClient;

    @Autowired
    private PolicyJobClient policyJobClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private AlarmTypeClient alarmTypeClient;

    @Autowired
    private PerformanceSettingApiClient performanceSettingApiClient;

    @GetMapping("/group-filter")
    @PreAuthorize("hasAuthority('API:DEVICE-GROUP:READ-LIST-DEVICE-GROUPS')")
    public String group_filter(Model model, @RequestParam Map<String, String> requestParams) throws UnsupportedEncodingException {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to group filter page","","");
        DeviceGroupPaginator deviceGroupPaginator = new DeviceGroupPaginator();
        deviceGroupPaginator.deviceGroupClient = deviceGroupClient;
        deviceGroupPaginator.indexParams = deviceGroupIndexParams();
        deviceGroupPaginator.parseParam((HashMap<String, String>) requestParams);
        deviceGroupPaginator.loadResult(requestParams);
        //add render args
        model.addAttribute("paginator", deviceGroupPaginator);
        loadDataSearch(model);
        return "policy/group_filter";
    }

    @PostMapping("/group-filter/create")
    @PreAuthorize("hasAuthority('API:DEVICE-GROUP:CREATE')")
    public String create(Model model, @RequestParam Map<String, String> requestParams) throws UnsupportedEncodingException {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create new group filter","","");
        Map<String, String> acsQuery = new HashMap<>();
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.manufacturer = requestParams.get(DevicePaginator.PAGE_MANUFACTURER);
        deviceGroup.modelName = requestParams.get(DevicePaginator.PAGE_MODEL_NAME);
        deviceGroup.firmwareVersion = requestParams.get(DevicePaginator.PAGE_FIRMWARE_VERSION);

        if (deviceGroup.manufacturer == null || ("").equals(deviceGroup.manufacturer))
            deviceGroup.manufacturer = "All";
        if (deviceGroup.modelName == null || ("").equals(deviceGroup.modelName))
            deviceGroup.modelName = "All";

        if (!("All").equals(deviceGroup.manufacturer) && !("All").equals(deviceGroup.modelName)) {
            DeviceTypeVersion[] deviceTypeVersion = deviceTypeVersionClient.findByManufacturerAndModelName(deviceGroup.manufacturer, deviceGroup.modelName);
            if (deviceTypeVersion != null && deviceTypeVersion.length > 0) {
                deviceGroup.oui = deviceTypeVersion[0].oui;
                deviceGroup.productClass = deviceTypeVersion[0].productClass;
                requestParams.put(OUI, deviceGroup.oui);
                requestParams.put(PRODUCT_CLASS, deviceGroup.productClass);
            }
        }

        if (deviceGroup.firmwareVersion == null || ("").equals(deviceGroup.firmwareVersion))
            deviceGroup.firmwareVersion = null;

        deviceGroup.name = requestParams.get("filterName");
        deviceGroup.label = requestParams.get(DevicePaginator.PAGE_SEARCH_LABEL);

        if (requestParams.get("groupId") != null && !("").equals(requestParams.get("groupId"))) {
            deviceGroup.id = Long.parseLong(requestParams.get("groupId"));
            deviceGroupClient.update(deviceGroup.id, deviceGroup);
        } else {
            deviceGroupClient.create(deviceGroup);
        }
        return "redirect:/group-filter";
    }

    @PostMapping("/group-filter/remove/{id}")
    @PreAuthorize("hasAuthority('API:DEVICE-GROUP:DELETE')")
    @ResponseBody
    public  AcsResponse removeGroup(Model model, @PathVariable("id") long id) throws UnsupportedEncodingException {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete group filter "+id,"","");
        AcsResponse result = new AcsResponse();
            try {
                deviceGroupClient.delete(id);
                result.mapResult.put(id+"", String.valueOf(200));
            } catch (Exception ex) {
                result.mapResult.put(id+"", String.valueOf(404));
            }
        return result;
    }

    @PostMapping("/group-filter/{filterName}/checkFilter")
    @PreAuthorize("hasAuthority('API:DEVICE-GROUP:READ-ONE-DEVICE-GROUP')")
    @ResponseBody
    public  AcsResponse checkFilter(Model model, @PathVariable("filterName") String filterName) throws UnsupportedEncodingException {
        AcsResponse result = new AcsResponse();
        try {
            DeviceGroup[] deviceGroups = deviceGroupClient.findByName(filterName);
            if(deviceGroups.length > 0) result.mapResult.put(filterName, String.valueOf(200));
        } catch (Exception ex) {
            result.mapResult.put(filterName, String.valueOf(404));
        }
        return result;
    }

    @PostMapping("/group-filter/{groupId}/checkJob")
    @PreAuthorize("hasAuthority('API:DEVICE-GROUP:READ-ONE-DEVICE-GROUP')")
    @ResponseBody
    public  AcsResponse checkJob(Model model, @PathVariable("groupId") String groupId) throws UnsupportedEncodingException {
        AcsResponse result = new AcsResponse();
        try {
            Boolean flag = true;
            Boolean policyJobses = policyJobClient.findJobExecute(groupId, "");
            User[] users = userClient.findUserByGroupID(groupId);
            AlarmType[] alarmTypes = alarmTypeClient.findAlarmTypeByGroupID(groupId);
            PerformanceSetting[] performanceSettings = performanceSettingApiClient.findPerformanceTypeByGroupID(groupId);
            String exits = "";

            if(policyJobses) {
                exits = exits + String.valueOf(404) + ",";
                flag = false;
            }
            if(users.length > 0) {
                exits = exits + String.valueOf(403) + ",";
                flag = false;
            }
            if(alarmTypes.length > 0) {
                exits = exits + String.valueOf(402) + ",";
                flag = false;
            }
            if(performanceSettings.length > 0) {
                exits = exits + String.valueOf(401) + ",";
                flag = false;
            }

            if(flag) result.mapResult.put(groupId, String.valueOf(200));
            else result.mapResult.put(groupId, exits.substring(0, exits.length() - 1));
        } catch (Exception ex) {
            logger.error("checkJob ", ex);
            result.mapResult.put(groupId, String.valueOf(404));
        }
        return result;
    }

    public int loadResult(String query) {
        // call webservice of backend
        Map<String, String> acsQuery = new HashMap<>();
        acsQuery.put("parameters", String.join(",", deviceIndexParams().keySet()));
        acsQuery.put("query", query);
        AcsResponse response = acsApiClient.findDevices(acsQuery);
        return response.nbOfItems;
    }

    private void loadDataSearch(Model model) {
        List<String> manufacturerlist = new ArrayList<String>();
        JsonObject response = checkListComboSearchDevice(null);
        if (response.size() > 0) {
            for (Map.Entry<String, JsonElement> entry : response.entrySet()) {
                manufacturerlist.add(entry.getKey());
            }
        }
        model.addAttribute("manufacturerlist", manufacturerlist);
        model.addAttribute("addNewFileParam", response.toString());
    }

}
