package vn.vnpt.ssdc.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.DeviceTypeClient;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.DeviceTypeDecorator;
import vn.vnpt.ssdc.models.DeviceTypeTree;
import vn.vnpt.ssdc.utils.ObjectUtils;

/**
 * Created by ThuHang on 10/11/2016.
 */
@Controller
public class DeviceTypeController extends BaseController{

    private static final String DEVICE_TYPE_PAGE = "device_types/device_type";

    @GetMapping("/device-types")
    public String index(Model model) {
        addBaseRenderArgs(model,null);
        return DEVICE_TYPE_PAGE;
    }

    @PostMapping("/device-types")
    public String create(Model model, @ModelAttribute DeviceTypeDecorator deviceTypeDecorator) {
        if(!this.deviceTypeExisted((long) 0,
                deviceTypeDecorator.toDeviceType().name,
                deviceTypeDecorator.toDeviceType().manufacturer,
                deviceTypeDecorator.toDeviceType().oui,
                deviceTypeDecorator.toDeviceType().productClass)) {
            DeviceType created = deviceTypeClient.create(deviceTypeDecorator.toDeviceType());
            return show(model,created.id);
        } else {
            model.addAttribute("errorMessage", "error.device.type.is.existed");
            return "fragments/error/500";
        }
    }

    @GetMapping("/device-types/{id}")
    public String show(Model model, @PathVariable Long id) {
        DeviceType deviceType = deviceTypeClient.get(id);
        addBaseRenderArgs(model, deviceType);
        model.addAttribute("deviceTypeModel",deviceType);
        model.addAttribute("deviceTypeId", id);
        return DEVICE_TYPE_PAGE;
    }

    @PostMapping("/device-types/{id}/update")
    public String update(Model model,@PathVariable("id") Long id, @ModelAttribute DeviceType deviceType) {
        deviceType.id = id;
        if(!this.deviceTypeExisted(deviceType.id, deviceType.name, deviceType.manufacturer, deviceType.oui, deviceType.productClass)) {
            deviceTypeClient.update(deviceType.id, deviceType);
        } else {
            model.addAttribute("errorMessage", "error.device.type.is.existed");
            return "fragments/error/500";
        }

        return show(model,deviceType.id);
    }

    private void addBaseRenderArgs(Model model, DeviceType currentDeviceType) {
        model.addAttribute("currentActiveHeader", HEADER_DATA_MODELS);
        model.addAttribute("deviceTypeDecorator",new DeviceTypeDecorator());

        DeviceTypeTree tree = new DeviceTypeTree(deviceTypeClient,false);
        tree.load();
        model.addAttribute("tree",tree);
        model.addAttribute("treeTitle","DEVICE TYPES");
        model.addAttribute("showAddDeviceType","true");
        if(tree.productClassesMap.size() > 0){
            model.addAttribute("flagShowNoDeviceType",false);
        }else{
            model.addAttribute("flagShowNoDeviceType",true);
        }
        model.addAttribute("deviceTypeModel",tree.firstDeviceType);
        if(!ObjectUtils.empty(currentDeviceType)) {
            model.addAttribute("currentDeviceType",currentDeviceType);
        } else {
            model.addAttribute("currentDeviceType",tree.firstDeviceType);
        }

    }

    @PostMapping("/device-types/{id}/delete")
    public String delete(Model model,@PathVariable("id") Long id, @ModelAttribute DeviceType deviceType) {
        deviceType.id = id;
        try{
            deviceTypeClient.delete(deviceType.id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "error.device.type.in.use");
            return "fragments/error/500";
        }
        return index(model);
    }

    @GetMapping("/device-types/existed")
    @ResponseBody
    public Boolean deviceTypeExisted(@RequestParam("id") Long id,
                                     @RequestParam("name") String name,
                                     @RequestParam("manufacturer") String manufacturer,
                                     @RequestParam("oui") String oui,
                                     @RequestParam("productClass") String productClass) {

        id = ObjectUtils.empty(id) ? 0 : id;
        return deviceTypeClient.isExisted(id, name, manufacturer, oui, productClass);
    }
}
