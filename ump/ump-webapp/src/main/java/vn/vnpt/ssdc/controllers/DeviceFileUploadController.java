package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.LoggingUserClient;
import vn.vnpt.ssdc.api.model.AcsResponse;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by Lamborgini on 3/1/2017.
 */
@Controller
public class DeviceFileUploadController extends BaseController{
    private static final String DEVICE_PAGE = "devices/fragments/device_file_upload";

    private Logger logger = LoggerFactory.getLogger(DeviceFileUploadController.class);

    @Autowired
    LoggingUserClient loggingUserClient;

    @GetMapping("/devices/device_file_upload/{deviceId}")
    @PreAuthorize("hasAuthority('API:ACS:UPLOAD-FILE')")
    public String index(Model model, @PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to device file upload page","","");
        model.addAttribute("deviceID", deviceId);
        return DEVICE_PAGE;
    }

    @PostMapping("/devices/device_file_upload/send/{deviceId}/{fileType}/{now}")
    @PreAuthorize("hasAuthority('API:ACS:UPLOAD-FILE')")
    @ResponseBody
    public AcsResponse send(Model model, @PathVariable("deviceId") String deviceId,
                     @PathVariable("fileType") String fileType,
                     @PathVariable("now") Boolean now) throws UnsupportedEncodingException {
        AcsResponse result = acsApiClient.uploadFile(deviceId, now, fileType);
        if(result.httpResponseCode == 202 || result.httpResponseCode == 200) {
            result.mapResult.put(deviceId, String.valueOf(200));
        } else result.mapResult.put(deviceId, String.valueOf(404));
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"upload",deviceId,loggingUserClient.getTaskId(result));
        return result;
    }

}
