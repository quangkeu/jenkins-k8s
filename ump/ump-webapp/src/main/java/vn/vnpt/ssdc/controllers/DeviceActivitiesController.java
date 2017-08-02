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
import vn.vnpt.ssdc.api.client.ParameterDetailClient;
import vn.vnpt.ssdc.api.client.PolicyTaskClient;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.LoggingDeviceActivity;
import vn.vnpt.ssdc.models.Device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DeviceActivitiesController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(DeviceActivitiesController.class);
    private final String INDEX_URL = "/devices/device_activity";
    private final String INDEX_TEMPLATE = "devices/fragments/device_activity";

    @Autowired
    protected ParameterDetailClient parameterDetailClient;

    @Autowired
    PolicyTaskClient policyTaskClient;


    @GetMapping(INDEX_URL + "/{deviceId}")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    public String index(Model model,
                        @PathVariable("deviceId") String deviceId,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "20") int limit,
                        @RequestParam(value = "fromDateTime", defaultValue = "") String fromDateTime,
                        @RequestParam(value = "toDateTime", defaultValue = "") String toDateTime,
                        @RequestParam Map<String, String> request) {
        logger.info("#USER_LOG {},{},{},{},{}", session.getId(), session.getAttribute("username"), "go to activity page", deviceId, "");

        // Get device info
        Device currentDevice = new Device();
        Map<String, String> indexParams = indexParamsDetail();
        Map<String, String> acsQuery = new HashMap<>();
        acsQuery.put("query", String.format("{\"_id\":\"%s\"}", deviceId));
        acsQuery.put("parameters", String.join(",", indexParams.keySet()));
        AcsResponse response = acsApiClient.findDevices(acsQuery);
        List<Device> devices = Device.fromJsonString(response.body, indexParams.keySet());
        if (devices.size() > 0) {
            currentDevice = devices.get(0);
        }

        // Get device activity
        LoggingDeviceActivity[] loggingDeviceActivities = policyTaskClient.getPageDeviceActivity(deviceId, fromDateTime, toDateTime, page, limit);

        // Convert parameter data
        for (LoggingDeviceActivity loggingDeviceActivity : loggingDeviceActivities) {
            loggingDeviceActivity.convertDataToView(acsApiClient);
        }

        // Get summary
        Map<String, Long> dataSummary = policyTaskClient.getSummaryDeviceActivity(deviceId, fromDateTime, toDateTime);
        Long totalPages = (long) Math.floor(dataSummary.get("totalElements") / limit) + 1;
        totalPages = page > totalPages ? Long.valueOf(page) : totalPages;
        dataSummary.put("totalPages", totalPages);

        model.addAttribute("loggingDeviceActivities", loggingDeviceActivities);
        model.addAttribute("summary", dataSummary);
        model.addAttribute("currentDevice", currentDevice);
        model.addAttribute("request", request);
        return INDEX_TEMPLATE;
    }

    @PostMapping(INDEX_URL + "/delete")
    @ResponseBody
    public Boolean deleteActivity(@RequestParam(value = "ids", defaultValue = "") String ids) {
        Boolean result = false;
        try {
            for (String id : ids.split(",")) {
                result = policyTaskClient.deleteDeviceActivity(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


}