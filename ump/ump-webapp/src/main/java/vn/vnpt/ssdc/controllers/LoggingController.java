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
import vn.vnpt.ssdc.api.client.ConfigurationClient;
import vn.vnpt.ssdc.api.client.LoggingDeviceClient;
import vn.vnpt.ssdc.api.client.LoggingUserClient;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.LoggingDevice;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.models.LoggingUserPaginator;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Controller
public class LoggingController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(DataModelController.class);
    private static final String LOGGING_DEVICE_PAGE = "logging/logging_device";
    private static final String LOGGING_USER_PAGE = "logging/logging_user";

    private static final String LOGGING_URL = "/logging";
    private static final String LOGGING_USER_URL = "/logging/user";
    private static final String LOGGING_DEVICE_URL = "/logging/device";


    @Autowired
    private LoggingDeviceClient loggingDeviceClient;

    @Autowired
    private ConfigurationClient configurationClient;

    @Autowired
    private LoggingUserClient loggingUserClient;

    //<editor-fold desc="LOGGING DEVICE">
    @GetMapping(LOGGING_DEVICE_URL)
    @PreAuthorize("hasAuthority('API:DEVICE-LOG:GET-PAGE')")
    public String logging(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "limit", defaultValue = "20") int limit,
                          @RequestParam(value = "name", defaultValue = "") String name,
                          @RequestParam(value = "actor", defaultValue = "") String actor,
                          @RequestParam(value = "fromDate", defaultValue = "") String fromDateTime,
                          @RequestParam(value = "toDate", defaultValue = "") String toDateTime,
                          @RequestParam Map<String, String> request) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to device log page","","");

        String username = session.getAttribute("username").toString();
        LoggingDevice[] loggingDevices = loggingDeviceClient.getPage(page, limit, name, actor, fromDateTime, toDateTime, username);
        Long totalPages = loggingDeviceClient.getTotalPages(page, limit, name, actor, fromDateTime, toDateTime, username);

        if (Long.valueOf(page) > totalPages) {
            totalPages = totalPages > 0 ? totalPages : 1;
            request.put("page", String.valueOf(totalPages));
            loggingDevices = loggingDeviceClient.getPage(Math.toIntExact(totalPages), limit, name, actor, fromDateTime, toDateTime, username);
        }

        model.addAttribute("loggingDevices", loggingDevices);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("request", request);

        return LOGGING_DEVICE_PAGE;
    }

    @GetMapping(LOGGING_DEVICE_URL + "/export/{cwmpSession}")
    @PreAuthorize("hasAuthority('API:DEVICE-LOG:EXPORT')")
    public void exportLoggingDeviceXml(@PathVariable String cwmpSession, HttpServletResponse response) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"export device log","","");
        loggingDeviceClient.exportXML(cwmpSession, response);
    }

    @PostMapping(LOGGING_DEVICE_URL + "/remove-all")
    @PreAuthorize("hasAuthority('API:DEVICE-LOG:REMOVE-ALL')")
    @ResponseBody
    public boolean removeAllLoggingDevice(@RequestParam(value = "name", defaultValue = "") String name,
                                          @RequestParam(value = "actor", defaultValue = "") String actor,
                                          @RequestParam(value = "fromDate", defaultValue = "") String fromDateTime,
                                          @RequestParam(value = "toDate", defaultValue = "") String toDateTime) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove all device log","","");
        return loggingDeviceClient.removeAll(name, actor, fromDateTime, toDateTime);
    }

    @GetMapping(LOGGING_DEVICE_URL + "/search-device")
    @ResponseBody
    public Set<String> searchDevice(@RequestParam(value = "deviceId", defaultValue = "") String deviceId) {

        Set<String> deviceIds = new TreeSet<String>();

        // Get device info
        Device currentDevice = null;
        Map<String, String> indexParams = indexParamsDetail();
        Map<String, String> acsQuery = new HashMap<>();
        acsQuery.put("query", String.format("{\"_id\":\"/.*%s.*/\"}", deviceId));
        acsQuery.put("parameters", String.join(",", indexParams.keySet()));
        AcsResponse response = acsApiClient.findDevices(acsQuery);
        List<Device> devices = Device.fromJsonString(response.body, indexParams.keySet());

        // Add string to response
        for (Device device : devices) {
            deviceIds.add(device.id);
        }

        return deviceIds;
    }

    //</editor-fold>

    //<editor-fold desc="LOGGING">
    @PostMapping(LOGGING_URL + "/post-save-time-expire")
    @PreAuthorize("hasAuthority('API:DEVICE-LOG:SAVE-TIME-EXPIRE')")
    @ResponseBody
    public boolean postSaveTimeExpire(@RequestParam(value = "timeExpire", defaultValue = "") String timeExpire) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"setting time expire for device log","","");
        Boolean result = true;
        result = loggingDeviceClient.saveTimeExpire(timeExpire);
        
        return result;
    }

    @GetMapping(LOGGING_URL + "/get-time-expire")
    @PreAuthorize("hasAuthority('API:DEVICE-LOG:SAVE-TIME-EXPIRE')")
    @ResponseBody
    public String getTimeExpire() {
        String result = null;

        try {
            result = configurationClient.get("timeExpire").value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    //</editor-fold>

    //<editor-fold desc="LOGGING USER">
    @GetMapping(LOGGING_USER_URL)
    @PreAuthorize("hasAuthority('API:USER-LOG:READ-LIST-USER-LOG')")
    public String listLoggingUser(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "limit", defaultValue = "20") int limit,
                          @RequestParam(value = "name", defaultValue = "") String name,
                          @RequestParam(value = "actor", defaultValue = "") String actor,
                          @RequestParam(value = "fromDate", defaultValue = "") String fromDateTime,
                          @RequestParam(value = "toDate", defaultValue = "") String toDateTime,
                          @RequestParam Map<String, String> request) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to user log page","","");

        page = page > 1 ? page - 1 : 0;
        LoggingUserPaginator loggingUserPaginator = loggingUserClient.getPage(page, limit, name, actor, fromDateTime, toDateTime);

        model.addAttribute("loggingUserPaginator", loggingUserPaginator);
        model.addAttribute("request", request);

        return LOGGING_USER_PAGE;
    }

    @PostMapping(LOGGING_USER_URL + "/remove-all")
    @PreAuthorize("hasAuthority('API:USER-LOG:REMOVE-ALL')")
    @ResponseBody
    public boolean removeAllLoggingUser(@RequestParam(value = "name", defaultValue = "") String name,
                                        @RequestParam(value = "actor", defaultValue = "") String actor,
                                        @RequestParam(value = "fromDate", defaultValue = "") String fromDateTime,
                                        @RequestParam(value = "toDate", defaultValue = "") String toDateTime) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove all user log","","");
        Boolean result = true;
        result = loggingUserClient.removeAll(name, actor, fromDateTime, toDateTime);

        return result;
    }
    //</editor-fold>
}
