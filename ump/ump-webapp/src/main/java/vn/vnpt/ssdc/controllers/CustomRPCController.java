package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.LoggingUserClient;
import vn.vnpt.ssdc.api.client.RPCApiClient;
import vn.vnpt.ssdc.api.model.AcsResponse;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by thangnc on 08-Mar-17.
 */
@Controller
public class CustomRPCController extends BaseController {

    @Autowired
    protected RPCApiClient rpcApiClient;

    @Autowired
    LoggingUserClient loggingUserClient;

    private static final Logger logger = LoggerFactory.getLogger(CustomRPCController.class);
    private static final String requestGetRPC = "<cwmp:GetRPCMethod xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n</cwmp:GetRPCMethod>";;
    private static final String factoryReset = "<cwmp:FactoryReset xmls:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n</cwmp:FactoryReset>";

    @GetMapping("/devices/device_customRPC/{deviceId}/{method}")
    @PreAuthorize("hasAuthority('API:RPC:DOWNLOAD-FILE') OR hasAuthority('API:RPC:GET-PARAMETER-NAMES') OR hasAuthority('API:RPC:REBOOT') OR hasAuthority('API:RPC:UPLOAD-FILE')")
    public String customRPCNew(Model model,
                         @PathVariable("deviceId") String deviceId, @PathVariable("method") String method) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to custom rpc page","","");
        model.addAttribute("deviceID", deviceId);
        model.addAttribute("method", method);
        if(("GetRPCMethod").equals(method))  model.addAttribute("requestGetRPC", requestGetRPC);
        else model.addAttribute("requestGetRPC", "");
        if(("FactoryReset").equals(method))  model.addAttribute("factoryReset", factoryReset);
        else model.addAttribute("factoryReset", "");
        if(("Download").equals(method)) return "devices/fragments/device_customRPC_download";
        if(("SetParameterValues").equals(method) || ("GetParameterValues").equals(method) || ("GetParameterAttributes").equals(method))
            return "devices/fragments/device_customRPC_parameter_values";
        if(("SetParameterAttributes").equals(method)) return "devices/fragments/device_customRPC_parameter_attributes";
        return "devices/fragments/device_customRPC";
    }

    @GetMapping("/devices/device_customRPC/{deviceId}")
    @PreAuthorize("hasAuthority('API:RPC:DOWNLOAD-FILE') OR hasAuthority('API:RPC:GET-PARAMETER-NAMES') OR hasAuthority('API:RPC:REBOOT') OR hasAuthority('API:RPC:UPLOAD-FILE')")
    public String customRPC(Model model,
                            @PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to custom rpc page","","");
        model.addAttribute("deviceID", deviceId);
        model.addAttribute("method", "");
        return "devices/fragments/device_customRPC";
    }

    @PostMapping("/devices/device_customRPC/{deviceId}/{method}/{now}/send")
    @PreAuthorize("hasAuthority('API:RPC:DOWNLOAD-FILE') OR hasAuthority('API:RPC:GET-PARAMETER-NAMES') OR hasAuthority('API:RPC:REBOOT') OR hasAuthority('API:RPC:UPLOAD-FILE')")
    @ResponseBody
    public AcsResponse send(@PathVariable("deviceId") String deviceId,
                     @PathVariable("method") String method,
                     @PathVariable("now") Boolean now,
                     @RequestParam Map<String, String> requestParams) throws UnsupportedEncodingException {
        AcsResponse result = null;
        if(("addObject").equals(method)) {
            result = acsApiClient.addObject(deviceId, now, requestParams.get("object"), new HashMap<>());
        } else if(("deleteObject").equals(method)) {
            result = acsApiClient.deleteObject(deviceId, now, requestParams.get("object"));
        } else if(("FactoryReset").equals(method)) {
            result = acsApiClient.factoryReset(deviceId, now);
        } else if(("Reboot").equals(method)) {
            result = rpcApiClient.reboot(deviceId, requestParams.get("commandKey"), now);
        } else if(("GetParameterNames").equals(method)) {
            result = rpcApiClient.getParamterNames(deviceId, now, requestParams.get("object"), requestParams.get("level"));
        } else if(("Upload").equals(method)) {
            result = rpcApiClient.uploadFile(deviceId, now, requestParams.get("fileType"), requestParams.get("url"),
                    requestParams.get("username"), requestParams.get("password"), requestParams.get("delay"), requestParams.get("commandKey"));
        } else if(("Download").equals(method)) {
            result = rpcApiClient.downloadFile(deviceId, now, requestParams.get("fileType"), requestParams.get("url"),
                    requestParams.get("username"), requestParams.get("password"), requestParams.get("successURL"), requestParams.get("failureURL"),
                    requestParams.get("commandKey"), requestParams.get("fileSize"), requestParams.get("target"), requestParams.get("delay"), null, null, null);
        } else if(("SetParameterValues").equals(method)) {
            String[] tableParameter = requestParams.get("parameter").split(",");
            Map<String, Object> parameter_values = new HashMap<>();
            for (int i = 0; i < tableParameter.length; i++)
            {
                String parameter = tableParameter[i];
                parameter_values.put(parameter.split("\\|")[0], parameter.split("\\|")[1]);
            }
            result = acsApiClient.setParameterValues(deviceId, parameter_values);
        } else if(("GetParameterValues").equals(method)) {
            String[] tableParameter = requestParams.get("parameter").split(",");
            List<String> listParameter = new LinkedList<>();
            for(int i = 0; i < tableParameter.length; i++) {
                listParameter.add(tableParameter[i]);
            }
            result = acsApiClient.getParamterValues(deviceId, listParameter);
        } else if(("GetParameterAttributes").equals(method)) {
            String[] tableParameter = requestParams.get("parameter").split(",");
            result = new AcsResponse();
            result.httpResponseCode = 202;
        }  else if(("SetParameterAttributes").equals(method)) {
            String[] tableParameter = requestParams.get("parameter").split(",");
            result = new AcsResponse();
            result.httpResponseCode = 202;
        } else if(("GetRPCMethod").equals(method)) {
            result = new AcsResponse();
            result.httpResponseCode = 202;
        }
        if(result.httpResponseCode == 202 || result.httpResponseCode == 200) {
            result.mapResult.put(deviceId, String.valueOf(200));
        } else result.mapResult.put(deviceId, String.valueOf(404));

        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"send custom rpc "+method,deviceId,loggingUserClient.getTaskId(result));
        return result;
    }

    @PostMapping("/devices/device_customRPC/{deviceId}/{object}/checkObject")
    @PreAuthorize("hasAuthority('API:RPC:DOWNLOAD-FILE') OR hasAuthority('API:RPC:GET-PARAMETER-NAMES') OR hasAuthority('API:RPC:REBOOT') OR hasAuthority('API:RPC:UPLOAD-FILE')")
    @ResponseBody
    public AcsResponse checkObject(Model model, @PathVariable("deviceId") String deviceId
                                       , @PathVariable("object") String object) throws UnsupportedEncodingException {
        AcsResponse result = new AcsResponse();
        try {
            Boolean policyJobses = acsApiClient.checkObject(deviceId, object);
            if(policyJobses) result.mapResult.put(deviceId, String.valueOf(200));
            else result.mapResult.put(deviceId, String.valueOf(404));
        } catch (Exception ex) {
            logger.error("checkObject ", ex);
            result.mapResult.put(deviceId, String.valueOf(404));
        }
        return result;
    }

}
