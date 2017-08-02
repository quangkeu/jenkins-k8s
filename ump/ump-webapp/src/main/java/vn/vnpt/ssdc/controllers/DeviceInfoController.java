package vn.vnpt.ssdc.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.models.DeviceInfo;
import vn.vnpt.ssdc.models.DevicePaginator;
import vn.vnpt.ssdc.models.Label;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lamborgini on 3/1/2017.
 */
@Controller
public class DeviceInfoController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(DeviceInfoController.class);

    private static final String DEVICE_PAGE = "devices/fragments/device_info";

    @Autowired
    LoggingUserClient loggingUserClient;

    @Autowired
    private BlackListDeviceClient blackListDeviceClient;

    @Autowired
    private TagClient tagClient;

    @Autowired
    private AcsApiClient acsApiClient;

    @Autowired
    private DeviceTypeVersionClient deviceTypeVersionClient;

    @Autowired
    private SubscriberDeviceClient subscriberDeviceClient;

    @GetMapping("/devices/device_info/{deviceID}")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    public String index(Model model, @RequestParam Map<String, String> requestParams, @PathVariable("deviceID") String deviceID) {
//        String lastInform = "2017-03-01 06:30:39";
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to device info page",deviceID,"");
        loadData(model, requestParams, deviceID);
        return DEVICE_PAGE;
    }

    @GetMapping("/devices/device_info/{deviceID}/rebootDevice")
    @PreAuthorize("hasAuthority('API:ACS:REBOOT')")
    @ResponseBody
    public int rebootDevice(@PathVariable("deviceID") String deviceID) {
        int result;
        AcsResponse resultTmp = acsApiClient.reboot(deviceID, true);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"reboot device",deviceID,loggingUserClient.getTaskId(resultTmp));
        if (resultTmp.httpResponseCode == 200) {
            //success
            result = 200;
        } else if (resultTmp.httpResponseCode == 202) {
            //task queue
            result = 202;
        } else {
            //error
            result = 404;
        }

        return result;
    }

    @GetMapping("/devices/device_info/{deviceID}/factoryResetDevice")
    @PreAuthorize("hasAuthority('API:ACS:FACTORY-RESET')")
    @ResponseBody
    public int factoryResetDevice(@PathVariable("deviceID") String deviceID) {
        int result;
        AcsResponse resultTmp = acsApiClient.factoryReset(deviceID, true);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"factory reset device",deviceID,loggingUserClient.getTaskId(resultTmp));
        if (resultTmp.httpResponseCode == 200) {
            //success
            result = 200;
        } else if (resultTmp.httpResponseCode == 202) {
            //task queue
            result = 202;
        } else {
            //error
            result = 404;
        }
        return result;
    }

    @GetMapping("/devices/device_info/{deviceID}/deleteDevice/{mode}")
    @PreAuthorize("hasAuthority('API:ACS:DELETE-DEVICE')")
    @ResponseBody
    public boolean deleteDevices(@PathVariable("deviceID") String deviceID,
                                 @PathVariable("mode") String mode) {
        boolean response = false;
        if (deviceID != null && !deviceID.equals("")
                && mode != null && !mode.equals("")) {
            try {
                acsApiClient.deleteDevice(deviceID);
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete device",deviceID,"");
                if (!mode.equals("temporarily")) {
                    BlacklistDevice blacklistDevice = new BlacklistDevice();
                    blacklistDevice.setDeviceID(deviceID);
                    blackListDeviceClient.create(blacklistDevice);
                }
                response = true;
            } catch (Exception e) {
                response = false;
            }
        }

        return response;
    }

    @PostMapping("/devices/device_info/addTags")
    @PreAuthorize("hasAuthority('API:ACS:CREATE-LABEL')")
    @ResponseBody
    public int addTags(@RequestParam Map<String, String> params) {
        int response = 0;
        if (params.containsKey("device") && params.containsKey("tagName")) {
            String[] tagName = params.get("tagName").replace("@", "").split(",");
            for (int i = 0; i < tagName.length; i++) {
                if (tagName[i].contains("\"")) {
                    tagName[i] = tagName[i].replaceAll("\"", "");
                }
                response = acsApiClient.addLabel(params.get("device"), tagName[i]);
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"add label "+tagName[i],params.get("device"),"");
            }
        }
        return response;
    }

    @PostMapping("/devices/device_info/deleteLabel")
    @PreAuthorize("hasAuthority('API:ACS:REMOVE-LABEL')")
    @ResponseBody
    public boolean deleteLabel(@RequestParam Map<String, String> params) {
        boolean result = false;
        if (params.containsKey("device") && params.containsKey("tagName")) {
            String tagName = params.get("tagName");
            if (tagName.contains("\"")) {
                tagName = tagName.replaceAll("\"", "");
            }
            if (tagName.contains("\n")) {
                tagName = tagName.replaceAll("\n", "").trim();
            }
            result = acsApiClient.deleteLabel(params.get("device"), tagName);
            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete label "+tagName,params.get("device"),"");
        }
        return result;
    }

    @GetMapping("/devices/device_info/{oldDevice}/{newDevice}/replaceCPE")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-DEVICE:REPLACE-CPE')")
    @ResponseBody
    public String replaceCPE(@PathVariable("oldDevice") String oldDevice,
                             @PathVariable("newDevice") String newDevice) {
        String result = "";
        if (oldDevice != null && !oldDevice.equals("")
                && newDevice != null && !newDevice.equals("")) {
            boolean check = subscriberDeviceClient.countSubscriberDeviceByDeviceId(newDevice);
            if (!check) {
                boolean b = subscriberDeviceClient.replaceCPE(oldDevice, newDevice);
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"replace "+oldDevice+" to "+newDevice,oldDevice+","+newDevice,"");
                if (b) {
                    result = "successful";
                } else {
                    result = "Replace CPE Fail";
                }
            } else {
                result = "Device has subscriber";
            }
        }
        return result;
    }


    @GetMapping("/devices/device_info/{deviceId}/recheckStatus")
    @PreAuthorize("hasAuthority('API:ACS:RECHECK-STATUS')")
    @ResponseBody
    public boolean recheckStatus(@PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"recheck status",deviceId,"");
        boolean result = false;
        if (deviceId != null && !deviceId.equals("")) {
            result = acsApiClient.recheckStatus(deviceId);
        }
        return result;
    }

    @GetMapping("/devices/device_info/{deviceId}/{serial}/getDevices")
    @PreAuthorize("hasAuthority('API:ACS:GET-DEVICES')")
    @ResponseBody
    public String getDevices(@PathVariable("serial") String serial,
                             @PathVariable("deviceId") String deviceId) {
        JsonArray jsonArray = new JsonArray();
        if (serial != null && !serial.equals("")
                && deviceId != null && !deviceId.equals("")) {
            AcsResponse responseAll = acsApiClient.getDevices(serial);
            Map<String, String> deviceIndexParams = deviceIndexParams();
            List<Device> devices = Device.fromJsonString(responseAll.body, deviceIndexParams.keySet());

            for (int i = 0; i < devices.size(); i++) {
                if (!devices.get(i).id.equals(deviceId)) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", devices.get(i).id);
                    jsonArray.add(jsonObject);
                }
            }
        }
        return jsonArray.toString();
    }

    public void loadData(Model model, Map<String, String> requestParams, String deviceID) {
        Map<String, String> acsQueryAll = new HashMap<>();
        acsQueryAll.put("parameters", String.join(",", deviceInfoIndexParams().keySet()));
        acsQueryAll.put("query", "{\"_id\":\"/" + deviceID + "/\"}");
        AcsResponse responseAll = acsApiClient.findDevices(acsQueryAll);
        List<DeviceInfo> deviceInfos = Device.fromJsonStringDeviceInfo(responseAll.body, deviceInfoIndexParams().keySet());
//        List<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
        List<String> deviceAllList = new ArrayList<String>();
//        for (DeviceInfo deviceInfo: deviceList) {
//            if(deviceInfo.parameters.get("_id").equals(deviceID)) {
//                deviceInfos.add(deviceInfo);
//            }
//            else { deviceAllList.add(deviceInfo.parameters.get("_id"));}
//        }

        model.addAttribute("deviceInfo", deviceInfos.get(0));
        if (deviceInfos.get(0).isOnline()) {
            model.addAttribute("deviceOnline", "Online");
        } else {
            model.addAttribute("deviceOffline", "Possible reasons are:\n\r" +
                    "- Device may be offline or disconnected\n" +
                    "- TR-069 client might be disabled\n" +
                    "- Device is busy/overloaded\n" +
                    "- NAT traversal issues");
        }
        String[] tags = deviceInfos.get(0).parameters.get("_tags").split(",");
        List<Label> deviceTags = new ArrayList<Label>();
        List<String> listId = new ArrayList<String>();
        if (tags.length > 0) {

            for (int i = 0; i < tags.length; i++) {
                if (!tags[i].equals("")) {
                    Label label = new Label();

                    int result;
                    do {
                        Random r = new Random();
                        result = r.nextInt(100 - 10) + 10;
                    } while (listId.contains(String.valueOf(result)));

                    label.aId = "a" + String.valueOf(result);
                    label.sId = "s" + String.valueOf(result);
                    label.name = tags[i];
                    deviceTags.add(label);
                    listId.add(String.valueOf(result));
                }
            }
        }

        DeviceTypeVersion byDevice = deviceTypeVersionClient.findByDevice(deviceID);
        Tag[] provisioningTagByDeviceTypeVersion = new Tag[]{};
        if (byDevice != null) {
            provisioningTagByDeviceTypeVersion = tagClient.getProvisioningTagByDeviceTypeVersion(byDevice.getId());
        }
        List<String> deviceProvisionings = new ArrayList<String>();
        if (provisioningTagByDeviceTypeVersion.length > 0) {
            for (int i = 0; i < provisioningTagByDeviceTypeVersion.length; i++) {
                if (!provisioningTagByDeviceTypeVersion[i].equals("")) {
                    deviceProvisionings.add(provisioningTagByDeviceTypeVersion[i].name);
                }
            }
        }

        model.addAttribute("deviceTags", deviceTags);
        model.addAttribute("deviceTagsSize", listId.toString());
        model.addAttribute("deviceProvisionings", deviceProvisionings);
        model.addAttribute("deviceProvisioningSize", deviceProvisionings.size());
        model.addAttribute("deviceID", deviceID);
        model.addAttribute("deviceAllList", deviceAllList);

    }

    protected Map<String, String> deviceIndexParams() {
        return new LinkedHashMap<String, String>() {{
            put("_id", "_id");
        }};
    }

    @GetMapping("/devices/device_info/{ipDevice}/pingDevice")
    @ResponseBody
    public String pingDevice(@PathVariable("ipDevice") String ipDevice) {
        return deviceTypeVersionClient.pingDevices(ipDevice);
    }
}
