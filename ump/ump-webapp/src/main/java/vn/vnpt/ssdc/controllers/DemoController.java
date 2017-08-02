package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;

import java.util.Map;

/**
 * Created by THANHLX on 2/14/2017.
 */
@Controller
public class DemoController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/data-model/list-all")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findAll();
        for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions){
            DeviceType deviceType = deviceTypeClient.get(deviceTypeVersion.getDeviceTypeId());
            deviceTypeVersion.setManufacturer(deviceType.manufacturer);
            deviceTypeVersion.setOui(deviceType.oui);
            deviceTypeVersion.setProductClass(deviceType.productClass);
        }
        model.addAttribute("deviceTypeVersions",deviceTypeVersions);
        return "demo/device_type_version";
    }



}
