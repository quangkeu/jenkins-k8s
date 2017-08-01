package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.vnpt.ssdc.api.client.DeviceTypeClient;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.controllers.decorator.DeviceTypeDecorator;
import vn.vnpt.ssdc.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static vn.vnpt.ssdc.utils.Constants.*;

/**
 * Created by vietnq on 11/2/16.
 */
@Controller
public class DeviceTypeController {
    private Logger logger = LoggerFactory.getLogger(DeviceTypeController.class);

    @Autowired
    private DeviceTypeClient deviceTypeClient;

    @GetMapping("/device-types")
    public String index(Model model) {
        model.addAttribute("deviceTypeNodes",deviceTypeNodes());
        model.addAttribute("deviceTypeDecorator",new DeviceTypeDecorator());
        model.addAttribute("currentActiveHeader",HEADER_DEVICE_TYPES);
        return "device_type";
    }

    @PostMapping("/device-types")
    public String create(Model model, @ModelAttribute DeviceTypeDecorator deviceTypeDecorator) {
        DeviceType created = deviceTypeClient.create(deviceTypeDecorator.toDeviceType());
        return show(model,created.id);
    }

    @GetMapping("/device-types/{id}")
    public String show(Model model, @PathVariable Long id) {
        logger.info("show details for device type #{}",id);
        DeviceType deviceType = deviceTypeClient.get(id);
        model.addAttribute("currentDeviceType",deviceType);

        //information for device types tree (sidebar)
        model.addAttribute("deviceTypeNodes",deviceTypeNodes());
        model.addAttribute("deviceTypeDecorator",new DeviceTypeDecorator());
        model.addAttribute("currentActiveHeader","device-types");
        return "device_type";
    }

    /*
     * returns a map to display a tree with
     */
    private Map<String,Map<String,DeviceType>> deviceTypeNodes() {
        Map<String,Map<String,DeviceType>> map = new HashMap<String,Map<String,DeviceType>>();

        DeviceType[] deviceTypes = deviceTypeClient.findAll();
        for(DeviceType deviceType : deviceTypes) {
            String keyL1 = deviceType.manufacturer + " - " + deviceType.oui;
            String keyL2 = deviceType.name + " - " + deviceType.productClass;
            if(!map.containsKey(keyL1)) {
                map.put(keyL1, new HashMap<String,DeviceType>());
            }
            if(!ObjectUtils.empty(deviceType.firmwareVersion)) {
                map.get(keyL1).put(keyL2,deviceType);
            }
        }
        return map;
    }

    private Map<String,List<String>> getDiffWithPreviousVersion(Long deviceTypeId) {
        Map<String,List<String>> map = new HashMap<String,List<String>>();
        map.put(NEW_PARAMETERS, new ArrayList<String>());
        map.put(REMOVED_PARAMETERS, new ArrayList<String>());

        DeviceType current = deviceTypeClient.get(deviceTypeId);
        DeviceType prev = deviceTypeClient.prev(deviceTypeId);

        if(!ObjectUtils.empty(current) && !ObjectUtils.empty(prev)) {
            for(String parameter : current.parameters.keySet()) {
                if(!prev.parameters.containsKey(parameter)) {
                    map.get(NEW_PARAMETERS).add(parameter);
                }
            }

            for(String parameter : prev.parameters.keySet()) {
                if(!current.parameters.containsKey(parameter)) {
                    map.get(REMOVED_PARAMETERS).add(parameter);
                }
            }
        }
        return map;
    }
}
