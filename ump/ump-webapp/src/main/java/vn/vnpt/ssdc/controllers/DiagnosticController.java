package vn.vnpt.ssdc.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import vn.vnpt.ssdc.api.client.DiagnosticApiClient;
import vn.vnpt.ssdc.api.client.DiagnosticTaskApiClient;
import vn.vnpt.ssdc.api.model.*;
import vn.vnpt.ssdc.models.Device;
import vn.vnpt.ssdc.models.DevicePaginator;
import vn.vnpt.ssdc.models.DiagnosticPaginator;
import vn.vnpt.ssdc.models.DiagnosticParameter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SSDC on 11/14/2016.
 */

@Controller
public class DiagnosticController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(DiagnosticController.class);
    @Autowired
    private DiagnosticApiClient diagnosticApiClient;

    @Autowired
    private DiagnosticTaskApiClient diagnosticTaskApiClient;

    private static final String DEVICE_ID_KEY = "deviceId";

    @GetMapping("/devices/device_diagnostics/{deviceId}")
    @PreAuthorize("hasAuthority('API:DIAGNOSTIC:GET-ALL-TASK')")
    public String diagnostics(Model model, @RequestParam Map<String, String> requestParams,
                              @PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to diagnostics page",deviceId,"");
        DiagnosticPaginator diagnosticPaginator = new DiagnosticPaginator();
        diagnosticPaginator.diagnosticApiClient = diagnosticApiClient;
        diagnosticPaginator.indexParams = deviceDiagnosticsIndexParams();
        diagnosticPaginator.parseParam((HashMap<String, String>) requestParams);
        diagnosticPaginator.loadResult(requestParams, deviceId, "");
        //add render args
        model.addAttribute("paginator", diagnosticPaginator);
        model.addAttribute("showAddTask", "true");
        model.addAttribute("deviceID", deviceId);
        return "devices/device_diagnostics";
    }

    @GetMapping("/devices/device_diagnostics/{deviceId}/{tab}")
    @PreAuthorize("hasAuthority('API:DIAGNOSTIC:GET-ALL-TASK')")
    public String diagnostics(Model model, @RequestParam Map<String, String> requestParams,
                              @PathVariable("deviceId") String deviceId,
                              @PathVariable("tab") String tab) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"move to diagnostics tab (requested, completed, errors)",deviceId,"");
        DiagnosticPaginator diagnosticPaginator = new DiagnosticPaginator();
        diagnosticPaginator.diagnosticApiClient = diagnosticApiClient;
        diagnosticPaginator.indexParams = deviceDiagnosticsIndexParams();
        diagnosticPaginator.parseParam((HashMap<String, String>) requestParams);
        diagnosticPaginator.loadResult(requestParams, deviceId, tab);
        //add render args
        model.addAttribute("paginator", diagnosticPaginator);
        model.addAttribute("showAddTask", "true");
        model.addAttribute("deviceID", deviceId);
        return "devices/device_diagnostics";
    }

    @GetMapping("/devices/device_diagnostics/create/{deviceId}")
    @PreAuthorize("hasAuthority('API:DIAGNOSTIC:CREATE-NEW')")
    public String createDiagnostics(Model model,
                                    @PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create new diagnostic task",deviceId,"");
        List<String> listInterface = diagnosticApiClient.getListInterface(deviceId);
        Device device = findDevice(deviceId.split("-")[2]);
        Map<String, List<DiagnosticParameter>> listParameter = new LinkedHashMap<>();
        List<Map<String, Tag>> listDiagnostics = diagnosticApiClient.getListDiagnostics(device.parameters.get(MANUFACTURER), deviceId.split("-")[0] , deviceId.split("-")[1], device.parameters.get(FIRMWARE_VERSION));
        if(listDiagnostics == null){
            listDiagnostics = new LinkedList<>();
        }
        for (Map<String, Tag> map : listDiagnostics) {
            List<DiagnosticParameter> parameters = new ArrayList<>();
            for (Map.Entry<String, Tag> entry : map.entrySet())
            {
                for(Map.Entry<String, Parameter> parameterEntry : entry.getValue().parameters.entrySet()){
                    Parameter parameter = parameterEntry.getValue();
                    DiagnosticParameter diagnosticParameter = new DiagnosticParameter();
                    diagnosticParameter.path = parameter.path;
                    diagnosticParameter.shortName = parameter.shortName;
                    diagnosticParameter.access = parameter.access;
                    diagnosticParameter.diagnosticKey = entry.getKey();
                    diagnosticParameter.inputName = entry.getKey() + " " + parameter.path;
                    diagnosticParameter.dataType = parameter.dataType;

                    String rule = parameter.rule;
                    List<String> values = new LinkedList<>();
                    if(rule != null) {
                        rule = rule.substring(1 , rule.length() - 1);
                        if(rule.contains("<")) {
                            diagnosticParameter.max = rule.substring(1, rule.length());
                            diagnosticParameter.min = null;
                        }
                        if(rule.contains(">")) {
                            diagnosticParameter.max = null;
                            diagnosticParameter.min = rule.substring(1, rule.length());
                        }
                        if(rule.startsWith("-") && !rule.contains("<")) {
                            rule = rule.substring(1, rule.length());
                            diagnosticParameter.max = rule.split("-")[1];
                            diagnosticParameter.min = "-" + rule.split("-")[0];
                        }
                        if(rule.contains("-") && !rule.startsWith("-")) {
                            diagnosticParameter.max = rule.split("-")[1];
                            diagnosticParameter.min = rule.split("-")[0];
                        }
                        if(rule.contains(";")) {
                            String[] rules = rule.split(";");
                            for(int i = 0; i < rules.length; i++) {
                                values.add(rules[i]);
                            }
                        }
                        diagnosticParameter.values = values;

                    }
                    parameters.add(diagnosticParameter);
                }
                listParameter.put(entry.getKey(), parameters);
            }
        }
        model.addAttribute("listInterface", listInterface);
        model.addAttribute("deviceID", deviceId);
        model.addAttribute("listParameter", listParameter);

        return "devices/device_diagnostics_create";
    }

    @GetMapping("/devices/device_diagnostics/remove/{deviceId}/{id}")
    @PreAuthorize("hasAuthority('API:DIAGNOSTIC:CREATE-NEW')")
    public String removeDiagnostics(Model model,
                                    @PathVariable("deviceId") String deviceId,
                                    @PathVariable("id") String id) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove diagnostic task",deviceId,"");
        diagnosticApiClient.delete(id);
        return "redirect:/devices/device_diagnostics/" + deviceId;
    }

    @PostMapping("/devices/device_diagnostics/result/{deviceId}")
    public String createDiagnostics(Model model, @RequestParam Map<String, String> requestParams,
                                    @PathVariable("deviceId") String deviceId) {
        String diagnosticName = requestParams.get("type");
        Map<String, String> diagnosticParams = new HashMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet())
        {
            String key = entry.getKey();
            String tmpDiagnosticKey = key.split(" ")[0];
            if(tmpDiagnosticKey.equals(diagnosticName)) {
                String tmpPath = key.split(" ")[1];
                String value = entry.getValue();
                diagnosticParams.put(tmpPath, value);
            }
        }
        long id = diagnosticApiClient.insertDiagnosticsModel(deviceId, diagnosticName, diagnosticParams);
        return "redirect:/devices/device_diagnostics/result/" + id;
    }

    @GetMapping("/devices/device_diagnostics/result/{id}")
    public String createDiagnosticsById(Model model,
                                        @PathVariable("id") long id) {
        DiagnosticTask diagnostic = diagnosticTaskApiClient.get(id);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"view result of diagnostic task",diagnostic.deviceId,"");
        Device device = findDevice(diagnostic.deviceId.split("-")[2]);
        List<Map<String, Tag>> listDiagnostics = diagnosticApiClient.getListDiagnostics(device.parameters.get(MANUFACTURER), diagnostic.deviceId.split("-")[0] , diagnostic.deviceId.split("-")[1], device.parameters.get(FIRMWARE_VERSION));

        if(listDiagnostics == null){
            listDiagnostics = new LinkedList<>();
        }
        List<DiagnosticParameter> parameters = new ArrayList<>();
        for (Map<String, Tag> map : listDiagnostics) {

            for (Map.Entry<String, Tag> entry : map.entrySet())
            {
                if(diagnostic.diagnosticsName.equals(entry.getKey())) {
                for(Map.Entry<String, Parameter> parameterEntry : entry.getValue().parameters.entrySet()){
                    Parameter parameter = parameterEntry.getValue();
                    DiagnosticParameter diagnosticParameter = new DiagnosticParameter();
                    diagnosticParameter.path = parameter.path;
                    diagnosticParameter.shortName = parameter.shortName;
                    diagnosticParameter.access = parameter.access;
                    diagnosticParameter.diagnosticKey = entry.getKey();
                    diagnosticParameter.inputName = entry.getKey() + " " + parameter.path;
                    parameters.add(diagnosticParameter);
                }
                }
            }
        }

        if(diagnostic.result == null) model.addAttribute("requestParams", diagnostic.request);
        else model.addAttribute("requestParams", diagnostic.result);
        model.addAttribute("parameters", parameters);
        model.addAttribute("id", id);
        model.addAttribute("deviceID", diagnostic.deviceId);
        return "devices/device_diagnostics_result";
    }

    @PostMapping("/devices/device_diagnostics/remove")
    @ResponseBody
    public  AcsResponse removeGroup(Model model, @RequestBody String listGroupId) throws UnsupportedEncodingException {
        String lst = URLDecoder.decode(listGroupId, "UTF-8");
        AcsResponse result = new AcsResponse();
        String regex = "\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lst);
        while (matcher.find()) {
            String groupID = matcher.group();
            groupID = groupID.replaceAll("\"", "");
            try {
                diagnosticApiClient.delete(groupID);
                result.mapResult.put(groupID, String.valueOf(200));
            } catch (Exception ex) {
                result.mapResult.put(groupID, String.valueOf(404));
            }
        }
        return result;
    }

    /**
     * return a list Of avaiable interface
     *
     * @param deviceId
     * @return
     */
    private List<String> getListInterface(String deviceId) {
        return diagnosticApiClient.getListInterface(deviceId);
    }

    public Device findDevice(String serialNumber) {
        Device device = new Device();
        // call webservice of backend
        Map<String, String> acsQuery = new HashMap<>();
        acsQuery.put("parameters", String.join(",", deviceIndexParams().keySet()));
        StringBuilder sb = new StringBuilder("{");
        sb.append(String.format(",\"%s\":\"/%s/\"", SERIAL_NUMBER, serialNumber));
        sb.deleteCharAt(1);
        sb.append("}");
        acsQuery.put("query", sb.toString());
        AcsResponse response = acsApiClient.findDevices(acsQuery);
        List<Device> devices = Device.fromJsonString(response.body, deviceIndexParams().keySet());
        if(!devices.isEmpty()) device = devices.get(0);
        return device;
    }
}
