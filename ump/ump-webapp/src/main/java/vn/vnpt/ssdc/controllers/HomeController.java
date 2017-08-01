package vn.vnpt.ssdc.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.vnpt.ssdc.api.client.AcsApiClient;
import vn.vnpt.ssdc.dto.AcsResponse;
import vn.vnpt.ssdc.model.Device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static vn.vnpt.ssdc.utils.Constants.*;

/**
 * Created by vietnq on 10/20/16.
 */
@Controller
public class HomeController {

    @Autowired
    private AcsApiClient acsApiClient;

    @GetMapping("/")
    public String index(Model model) {
        Map<String, String> indexParams = indexParams();

        //query devices from acs
        Map<String, String> query = new HashMap<String, String>();
        query.put("parameters", String.join(",", indexParams.keySet()));
        //TODO add paging param

        AcsResponse response = acsApiClient.findDevices(query);

        List<Device> devices = Device.fromJsonString(response.body, indexParams.keySet());

        //add render args
        model.addAttribute("total", response.nbOfItems);
        model.addAttribute("indexParams", indexParams);
        model.addAttribute("devices", devices);
        model.addAttribute("currentActiveHeader",HEADER_DEVICES);
        return "index";
    }

    //returns a map containing friendly name and actual path for parameters shown in device index view
    //TODO move this to configuration
    private Map<String, String> indexParams() {
        return new HashMap<String, String>() {{
            put("_deviceId._SerialNumber", "Serial Number");
            put("_deviceId._Manufacturer", "Manufacturer");
            put("_deviceId._ProductClass", "Product Class");
            put("_lastInform", "Last Inform");
        }};
    }

    /**
     * Reboot device by device ID
     *
     * @param deviceId
     * @param timeout
     * @param now      :now = true then reboot right now
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    @GetMapping("/devices/{deviceId}/reboot")
    @ResponseBody
    public AcsResponse reboot(@PathVariable String deviceId,
                              @RequestParam(value = "timeout", defaultValue = "3000") String timeout,
                              @RequestParam(value = "now", defaultValue = "false") String now) {
        AcsResponse result = acsApiClient.reboot(deviceId, timeout, now);
        return result;
    }


    /**
     * Factory device by device ID
     *
     * @param deviceId
     * @param timeout
     * @param now      :now = true then factoryReset right now
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    @GetMapping("/devices/{deviceId}/factoryReset")
    @ResponseBody
    public AcsResponse factoryReset(@PathVariable String deviceId,
                                    @RequestParam(value = "timeout", defaultValue = "3000") String timeout,
                                    @RequestParam(value = "now", defaultValue = "false") String now) {
        return acsApiClient.factoryReset(deviceId, timeout, now);
    }

}
