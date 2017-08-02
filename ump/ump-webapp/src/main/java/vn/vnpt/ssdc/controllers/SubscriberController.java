package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.SubscriberClient;
import vn.vnpt.ssdc.api.client.SubscriberDeviceClient;
import vn.vnpt.ssdc.api.client.SubscriberTemplateClient;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.Subscriber;
import vn.vnpt.ssdc.api.model.SubscriberDevice;
import vn.vnpt.ssdc.api.model.SubscriberTemplate;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.models.SubscriberPaginator;
import vn.vnpt.ssdc.models.SubscriberTemplatePaginator;
import vn.vnpt.ssdc.subscriberdata.parser.SubscriberDataParserFactory;
import vn.vnpt.ssdc.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


@Controller
public class SubscriberController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(SubscriberController.class);

    private static final String SUBSCRIBERS_PAGE = "subscriber/subscriber";

    private static final String DATA_TEMPLATE_PAGE = "subscriber/data_template";

    @Autowired
    private SubscriberClient subscriberClient;

    @Autowired
    private SubscriberTemplateClient subscriberTemplateClient;

    @Autowired
    private SubscriberDataParserFactory subscriberDataParserFactory;

    @Autowired
    SubscriberDeviceClient subscriberDeviceClient;


    @GetMapping("/subscriber")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER:READ-LIST-SUBSCRIBER')")
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "20") int limit) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to subscriber page","","");
        page = page > 1 ? page - 1 : 0;
        SubscriberPaginator subscriberPaginator = subscriberClient.getPage(page, limit);
        subscriberPaginator.number++;
        String subscriberIds = "";
        for (Subscriber subscriber: subscriberPaginator.content) {
            subscriberIds += subscriber.subscriberId + ",";
        }

        SubscriberTemplate[] subscriberTemplates = subscriberTemplateClient.findAll();
        SubscriberDevice[] subscriberDevices = subscriberDeviceClient.findAll();
        Map<String, Set<SubscriberDevice>> subscriberDeviceMap = new LinkedHashMap<String, Set<SubscriberDevice>>();
        for (SubscriberDevice subscriberDevice: subscriberDevices) {
            Set<SubscriberDevice> subscribers = subscriberDeviceMap.get(subscriberDevice.subscriberId);
            if(ObjectUtils.empty(subscribers)) {
                subscribers = new LinkedHashSet<SubscriberDevice>();
            }
            subscribers.add(subscriberDevice);
            subscriberDeviceMap.put(subscriberDevice.subscriberId, subscribers);
        }

        model.addAttribute("subscriberPaginator", subscriberPaginator);
        model.addAttribute("subscriberTemplates", subscriberTemplates);
        model.addAttribute("subscriberDeviceMap", subscriberDeviceMap);

        return SUBSCRIBERS_PAGE;
    }

    @GetMapping("/subscriber/{id}")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER:READ-ONE-SUBSCRIBER')")
    @ResponseBody
    public Subscriber getSubscriber(@PathVariable("id") Long id) {
        return subscriberClient.get(id);
    }

    @GetMapping("/subscriber/{id}/get-form")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER:READ-ONE-SUBSCRIBER')")
    @ResponseBody
    public Map<String, Object> getFormSubscriber(@PathVariable("id") Long id) {
        Subscriber subscriber = subscriberClient.get(id);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"view subscriber "+subscriber.subscriberId,"","");
        SubscriberDevice[] subscriberDevices = subscriberDeviceClient.getSubscriberDeviceBySubscriberId(subscriber.subscriberId);

        // Get model name
        for (SubscriberDevice subscriberDevice : subscriberDevices) {
            Map<String, String> indexParams = indexParamsDetail();
            Map<String, String> acsQuery = new HashMap<>();
            acsQuery.put("query", String.format("{\"_deviceId._OUI\":\"%s\"," +
                            "\"_deviceId._ProductClass\":\"%s\"," +
                            "\"_deviceId._SerialNumber\":\"%s\"}",
                    subscriberDevice.oui, subscriberDevice.productClass, subscriberDevice.serialNumber));
            acsQuery.put("parameters", String.join(",", indexParams.keySet()));

            AcsResponse response = acsApiClient.findDevices(acsQuery);
            List<Device> devices = Device.fromJsonString(response.body, indexParams.keySet());

            if(!ObjectUtils.empty(devices) && devices.size() > 0) {
                Device device = devices.get(0);
                subscriberDevice.setModelName(device.modelName());
            }
        }

        SubscriberTemplate[] subscriberTemplates = subscriberTemplateClient.findAll();
        List<SubscriberTemplate> subscriberTemplatesResult = new ArrayList<SubscriberTemplate>();

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("subscriber", subscriber);
        response.put("subscriberDevice", subscriberDevices);
        response.put("subscriberTemplate", subscriberTemplates);

        return response;
    }


    @GetMapping("/subscriber/{id}/existed")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER:READ-ONE-SUBSCRIBER')")
    @ResponseBody
    public Boolean checkSubscriberExisted(@PathVariable("id") Long id,
                                          @RequestParam("subscriberId") String subscriberId) {
        Boolean check = false;
        Subscriber[] subscribers = subscriberClient.findAll();
        for(Subscriber subscriber: subscribers) {
            if(subscriber.subscriberId.toLowerCase().equals(subscriberId.toLowerCase()) && !subscriber.id.equals(id)) {
                check = true;
                break;
            }
        }

        return check;
    }

    @PostMapping("/subscriber/remove")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER:DELETE')")
    @ResponseBody
    public Boolean postRemoveSubscriber(@RequestParam Map<String, String> params) {
        for (String key : params.keySet()) {
            if(key.startsWith("ids")){
                Subscriber subscriber = subscriberClient.get(Long.valueOf(params.get(key)));
                if(!ObjectUtils.empty(subscriber)) {
                    SubscriberDevice[] subscriberDevices = subscriberDeviceClient.getSubscriberDeviceBySubscriberId(subscriber.subscriberId);
                    for (SubscriberDevice subscriberDevice: subscriberDevices) {
                        subscriberDeviceClient.delete(subscriberDevice.id);
                    }
                    subscriberClient.delete(Long.valueOf(params.get(key)));
                }
            }
        }
        return true;
    }


    @GetMapping("/data-template/get-list")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:READ-LIST-SUBSCRIBER-TEMPLATE')")
    @ResponseBody
    public List<SubscriberTemplate> getListDataTemplate(@RequestParam(value = "subscriberId", defaultValue = "0") Long id) {

        Subscriber subscriber = new Subscriber();
        if(id != 0) {
            subscriber = subscriberClient.get(id);
        }

        SubscriberTemplate[] subscriberTemplates = subscriberTemplateClient.findAll();
        List<SubscriberTemplate> subscriberTemplatesResult = new ArrayList<SubscriberTemplate>();

        for (SubscriberTemplate subscriberTemplate: subscriberTemplates) {
            if(!ObjectUtils.empty(subscriberTemplate) && !subscriber.subscriberDataTemplateIds.contains(String.valueOf(id))) {
                subscriberTemplatesResult.add(subscriberTemplate);
            }
        }

        return subscriberTemplatesResult;
    }

    @GetMapping("/subscriber/get-device")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-DEVICE:FIND-BY-SUBSCRIBER-ID')")
    @ResponseBody
    public Map<String, Object> getDevices(@RequestParam("oui") String oui,
                                          @RequestParam("productClass") String productClass,
                                          @RequestParam("serialNumber") String serialNumber) {

        Map<String, Object> response = new HashMap<>();

        String deviceId = oui + "-" + productClass + "-" + serialNumber;
        Device device = null;
        Boolean isUse = false;

        if(!subscriberDeviceClient.countSubscriberDeviceByDeviceId(deviceId)) {
            Map<String, String> indexParams = indexParamsDetail();
            Map<String, String> acsQuery = new HashMap<>();
            acsQuery.put("query", String.format("{\"_deviceId._OUI\":\"%s\"," +
                            "\"_deviceId._ProductClass\":\"%s\"," +
                            "\"_deviceId._SerialNumber\":\"%s\"}",
                    oui, productClass, serialNumber));
            acsQuery.put("parameters", String.join(",", indexParams.keySet()));

            AcsResponse responseAcs = acsApiClient.findDevices(acsQuery);
            List<Device> devices = Device.fromJsonString(responseAcs.body, indexParams.keySet());

            if(!ObjectUtils.empty(devices) && devices.size() > 0) {
                device = devices.get(0);
            }
        } else {
            isUse = true;
        }

        response.put("object", device);
        response.put("isUse", isUse);

        return response;
    }

    @GetMapping("/subscriber/search-device")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-DEVICE:FIND-BY-SUBSCRIBER-ID')")
    @ResponseBody
    public List<Device> getDevices(@RequestParam(value = "q", defaultValue = "") String q) {

        String[] inputArr = q.split("-");
        List<Device> devices = new ArrayList<>();
        if(inputArr.length == 3) {
            Map<String, String> indexParams = indexParamsDetail();
            Map<String, String> acsQuery = new HashMap<>();
            acsQuery.put("query", String.format("{\"_deviceId._OUI\":\"*%s*\"," +
                            "\"_deviceId._ProductClass\":\"*%s*\"," +
                            "\"_deviceId._SerialNumber\":\"*%s*\"}",
                    inputArr[0], inputArr[1], inputArr[2]));
            acsQuery.put("parameters", String.join(",", indexParams.keySet()));

            AcsResponse response = acsApiClient.findDevices(acsQuery);
            devices = Device.fromJsonString(response.body, indexParams.keySet());
        }
        return devices;
    }

    @PostMapping("/subscriber/add-subscriber-device")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-DEVICE:CREATE')")
    @ResponseBody
    public Boolean addSubscriberDevice(@RequestParam("subscriberId") String subscriberId,
                              @RequestParam("oui") String oui,
                              @RequestParam("productClass") String productClass,
                              @RequestParam("serialNumber") String serialNumber) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"add device for subscriber "+subscriberId,"","");
        // Check is used
        String deviceId = oui + "-" + productClass + "-" + serialNumber;
        if(subscriberDeviceClient.countSubscriberDeviceByDeviceId(deviceId)) {
            return false;
        }

        SubscriberDevice subscriberDevice = new SubscriberDevice();
        subscriberDevice.subscriberId = subscriberId;

        Map<String, String> indexParams = indexParamsDetail();
        Map<String, String> acsQuery = new HashMap<>();
        acsQuery.put("query", String.format("{\"_deviceId._OUI\":\"%s\"," +
                        "\"_deviceId._ProductClass\":\"%s\"," +
                        "\"_deviceId._SerialNumber\":\"%s\"}",
                oui, productClass, serialNumber));
        acsQuery.put("parameters", String.join(",", indexParams.keySet()));

        AcsResponse response = acsApiClient.findDevices(acsQuery);
        List<Device> devices = Device.fromJsonString(response.body, indexParams.keySet());
        if(devices.size() > 0) {
            Device device = devices.get(0);
            subscriberDevice.deviceId = device.id;
            subscriberDevice.oui = device.oui();
            subscriberDevice.productClass = device.productClass();
            subscriberDevice.serialNumber = device.serialNumber();
            subscriberDevice.manufacturer = device.manufacturer();
        } else {
            subscriberDevice.deviceId = oui + "-" + productClass + "-" + serialNumber;
            subscriberDevice.oui = oui;
            subscriberDevice.productClass = productClass;
            subscriberDevice.serialNumber = serialNumber;
        }
        subscriberDeviceClient.create(subscriberDevice);

        return true;
    }

    @PostMapping("/subscriber/remove-subscriber-device/{id}")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-DEVICE:DELETE')")
    @ResponseBody
    public Boolean postRemoveSubscriberDevice(@PathVariable("id") Long id) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove device of subscriber","","");
        subscriberDeviceClient.delete(id);
        return true;
    }

    @PostMapping("/subscriber/{id}/remove-subscriber-template/{subscriberTemplateId}")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:DELETE')")
    @ResponseBody
    public Boolean postRemoveSubscriberTemplate(@PathVariable("id") Long id,
                                                @PathVariable("subscriberTemplateId") Long subscriberTemplateId) {

        Subscriber subscriber = subscriberClient.get(id);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove template of subscriber "+subscriber.subscriberId,"","");
        SubscriberTemplate subscriberTemplate  = subscriberTemplateClient.get(subscriberTemplateId);
        for (String key: subscriberTemplate.templateKeys){
            subscriber.subscriberData.remove(subscriberTemplate.name + "-" + key);
        }

        subscriber.subscriberDataTemplateIds.remove(String.valueOf(subscriberTemplateId));
        subscriberClient.update(subscriber.id, subscriber);

        return true;
    }

    @PostMapping("/subscriber/add-subscriber-template")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:CREATE')")
    @ResponseBody
    public Subscriber addSubscriberTemplate(@RequestParam("id") Long id,
                                                @RequestParam("subscriberTemplateId") Long subscriberTemplateId) {
        Subscriber subscriber = subscriberClient.get(id);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"add template for subscriber "+subscriber.subscriberId,"","");
        SubscriberTemplate subscriberTemplate = subscriberTemplateClient.get(subscriberTemplateId);
        for (String key: subscriberTemplate.templateKeys) {
            subscriber.subscriberData.put(subscriberTemplate.name + "-" + key, "");
        }
        subscriber.subscriberDataTemplateIds.add(String.valueOf(subscriberTemplateId));
        subscriberClient.update(subscriber.id, subscriber);

        return subscriber;
    }

    @PostMapping("/subscriber/{id}/save")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER:UPDATE')")
    public String save(@PathVariable("id") Long id,
                         @RequestParam("subscriberId") String subscriberId,
                         @RequestParam Map<String, String> params) {

        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update subscriber "+subscriberId,"","");
        Subscriber subscriber = new Subscriber();
        if(id > 0) {
            subscriber = subscriberClient.get(id);

            // Delete old record in subscriber_device
            SubscriberDevice[] subscriberDevices = subscriberDeviceClient.getSubscriberDeviceBySubscriberId(subscriber.subscriberId);
            for (SubscriberDevice subscriberDevice: subscriberDevices) {
                subscriberDeviceClient.delete(subscriberDevice.id);
            }
        }

        subscriber.subscriberId = subscriberId.trim();
        subscriber.subscriberDataTemplateIds = new LinkedHashSet<>();
        subscriber.subscriberData = new LinkedHashMap<String, String>();
        for (String key : params.keySet()) {
            if(!ObjectUtils.empty(key)) {
                if(key.startsWith("parameter_")) {
                    subscriber.subscriberData.put(key.substring(10, key.length()), params.get(key));
                }
                if(key.startsWith("subscriberTemplateId_")) {
                    subscriber.subscriberDataTemplateIds.add(params.get(key));
                }

                if(key.startsWith("deviceStr_")) {
                    String[] deviceStrArr = params.get(key).split("-");
                    if(deviceStrArr.length == 3) {
                        Map<String, String> indexParams = indexParamsDetail();
                        Map<String, String> acsQuery = new HashMap<>();
                        acsQuery.put("query", String.format("{\"_deviceId._OUI\":\"%s\"," +
                                        "\"_deviceId._ProductClass\":\"%s\"," +
                                        "\"_deviceId._SerialNumber\":\"%s\"}",
                                deviceStrArr[0], deviceStrArr[1], deviceStrArr[2]));
                        acsQuery.put("parameters", String.join(",", indexParams.keySet()));

                        AcsResponse response = acsApiClient.findDevices(acsQuery);
                        List<Device> devices = Device.fromJsonString(response.body, indexParams.keySet());

                        SubscriberDevice subscriberDevice = new SubscriberDevice();
                        subscriberDevice.subscriberId = subscriberId;
                        if(devices.size() > 0) {
                            subscriberDevice.deviceId = devices.get(0).id;
                            subscriberDevice.oui = devices.get(0).oui();
                            subscriberDevice.productClass = devices.get(0).productClass();
                            subscriberDevice.serialNumber = devices.get(0).serialNumber();
                            subscriberDevice.manufacturer = devices.get(0).manufacturer();
                        } else {
                            subscriberDevice.deviceId = deviceStrArr[0] + "-" + deviceStrArr[1] + "-" + deviceStrArr[2];
                            subscriberDevice.oui = deviceStrArr[0];
                            subscriberDevice.productClass = deviceStrArr[1];
                            subscriberDevice.serialNumber = deviceStrArr[2];
                        }
                        subscriberDeviceClient.create(subscriberDevice);
                    }
                }
            }
        }

        if(id > 0) {
            subscriberClient.update(id, subscriber);
        } else {
            subscriberClient.create(subscriber);
        }

        return "redirect:/subscriber";
    }

    @GetMapping("/subscriber/data-template")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:READ-LIST-SUBSCRIBER-TEMPLATE')")
    public String getListDataTemplate(Model model,
                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "limit", defaultValue = "20") int limit) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to subscriber data template page","","");
        page = page > 1 ? page - 1 : 0;
        SubscriberTemplatePaginator subscriberTemplatePaginator = subscriberTemplateClient.getPage(page, limit);
        subscriberTemplatePaginator.number++;
        model.addAttribute("subscriberTemplatePaginator", subscriberTemplatePaginator);

        return DATA_TEMPLATE_PAGE;
    }

    @PostMapping("/subscriber/data-template/{subscriberTemplateId}/post-remove-parameter")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:UPDATE')")
    @ResponseBody
    public Set<String> postRemoveParameter(@PathVariable("subscriberTemplateId") Long subscriberTemplateId,
                                           @RequestParam("parameterName") String parameterName) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update subscriber template "+subscriberTemplateId,"","");
        Set<String> response = new LinkedHashSet<String>();

        // Get list already in use
        Subscriber subscriber = subscriberClient.getBySubscriberTemplateId(subscriberTemplateId);
        if(subscriber != null) {
            SubscriberTemplate subscriberTemplate = subscriberTemplateClient.get(subscriberTemplateId);
            response.add(subscriberTemplate.name);
        }

        if(response.size() == 0) {
            SubscriberTemplate subscriberTemplate = subscriberTemplateClient.get(subscriberTemplateId);
            if(!ObjectUtils.empty(subscriberTemplate)) {
                for (String key: new LinkedHashSet<>(subscriberTemplate.templateKeys)) {
                    if(parameterName.equals(key)) {
                        subscriberTemplate.templateKeys.remove(key);
                    }
                }
                subscriberTemplateClient.update(subscriberTemplateId, subscriberTemplate);
            }
        }

        return response;
    }

    @PostMapping("/subscriber/data-template/get-name-remove")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:READ-ONE-SUBSCRIBER-TEMPLATE')")
    @ResponseBody
    public Set<String> getNameRemove(@RequestParam(value = "idsStr", defaultValue = "") String idsStr) {
        Set<String> response = new LinkedHashSet<>();
        for (String id: idsStr.split(",")) {
            response.add(subscriberTemplateClient.get(Long.valueOf(id)).name);
        }
        return response;
    }

    @PostMapping("/subscriber/data-template/remove")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:DELETE')")
    @ResponseBody
    public Set<String> postRemoveDataTemplate(@RequestParam Map<String, String> params) {

        Set<String> response = new LinkedHashSet<String>();

        // Get list already in use
        for (String key : params.keySet()) {
            if(key.startsWith("ids")){
                Long subscriberTemplateId = Long.valueOf(params.get(key));
                Subscriber subscriber = subscriberClient.getBySubscriberTemplateId(subscriberTemplateId);
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete subscriber template "+subscriberTemplateId,"","");
                if(subscriber != null) {
                    SubscriberTemplate subscriberTemplate = subscriberTemplateClient.get(subscriberTemplateId);
                    response.add(subscriberTemplate.name);
                }
            }
        }

        if(response.size() == 0) {
            for (String key : params.keySet()) {
                if(key.startsWith("ids")){
                    Long subscriberTemplateId = Long.valueOf(params.get(key));
                    subscriberTemplateClient.delete(subscriberTemplateId);
                }
            }
        }

        return response;
    }

    @GetMapping("/subscriber/data-template/{subscriberTemplateId}/existed")
    @ResponseBody
    public Boolean checkSubscriberTemplateExisted(@PathVariable("subscriberTemplateId") Long subscriberTemplateId,
                                                  @RequestParam("name") String name) {

        Boolean check = false;
        SubscriberTemplate[] subscriberTemplates = subscriberTemplateClient.findAll();
        for(SubscriberTemplate subscriberTemplate : subscriberTemplates) {
            if(subscriberTemplate.name.toLowerCase().equals(name.toLowerCase()) && !subscriberTemplate.id.equals(subscriberTemplateId)) {
                check = true;
                break;
            }
        }

        return check;
    }

    @PostMapping("subscriber/data-template/{subscriberTemplateId}/save")
    @PreAuthorize("hasAuthority('API:SUBSCRIBER-TEMPLATE:UPDATE')")
    @ResponseBody
    public Boolean saveSubscriberTemplate(@PathVariable("subscriberTemplateId") Long subscriberTemplateId,
                                          @RequestParam("name") String name,
                                          @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update subscriber template "+subscriberTemplateId,"","");
        if(!ObjectUtils.empty(subscriberTemplateId) && subscriberTemplateId > 0) {
            // Edit
            SubscriberTemplate subscriberTemplate = subscriberTemplateClient.get(subscriberTemplateId);
            subscriberTemplate.templateKeys = new LinkedHashSet<>();
            if(!ObjectUtils.empty(subscriberTemplate)) {
                subscriberTemplate.name = name.trim();
                for (String key: params.keySet()) {
                    if(key.startsWith("params")) {
                        String param = params.get(key);
                        subscriberTemplate.templateKeys.add(param.trim());
                    }
                }
                subscriberTemplateClient.update(subscriberTemplateId, subscriberTemplate);
            }

        } else {
            // Create
            SubscriberTemplate subscriberTemplate = new SubscriberTemplate();
            subscriberTemplate.name = name.trim();
            for (String key: params.keySet()) {
                if(key.startsWith("params")) {
                    String param = params.get(key);
                    subscriberTemplate.templateKeys.add(param.trim());
                }
            }
            subscriberTemplateClient.create(subscriberTemplate);
        }

        return true;
    }

    @GetMapping("/subscriber/data-template/{id}")
    @ResponseBody
    public Map<String, Object> postRemoveDataTemplate(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("object", subscriberTemplateClient.get(id));
        Boolean isUsed = false;
        Subscriber subscriber = subscriberClient.getBySubscriberTemplateId(id);
        if(subscriber != null) {
            isUsed = true;
        }

        response.put("isUsed", isUsed);
        return response;
    }
}
