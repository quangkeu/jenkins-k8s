package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.FileUploadClient;
import vn.vnpt.ssdc.api.client.LoggingUserClient;
import vn.vnpt.ssdc.api.model.AcsResponse;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by thangnc on 06-Mar-17.
 */
@Controller
public class DeviceFileDownloadController extends BaseController{

    private Logger logger = LoggerFactory.getLogger(DeviceFileDownloadController.class);

    @Autowired
    LoggingUserClient loggingUserClient;

    @Autowired
    private FileUploadClient fileUploadClient;

    @GetMapping("/devices/device_file_download/{deviceId}")
    @PreAuthorize("hasAuthority('API:ACS:DOWNLOAD-FILE')")
    public String diagnostics(Model model,
                              @PathVariable("deviceId") String deviceId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to device file download page","","");
        model.addAttribute("deviceID", deviceId);
        return "devices/fragments/device_file_download";
    }


    @PostMapping("/devices/device_file_download/getListFileVersions/{deviceId}/{fileType}")
    @PreAuthorize("hasAuthority('API:ACS:DOWNLOAD-FILE')")
    @ResponseBody
    public Map<String, String> getListFileVersions(Model model, @PathVariable("deviceId") String deviceId, @PathVariable("fileType") String fileType) throws UnsupportedEncodingException {
        Map<String, String> listFileVersions = fileUploadClient.getListFileVersions(deviceId, fileType);
        return listFileVersions;
    }

    @PostMapping("/devices/device_file_download/send/{deviceId}/{fileId}/{fileName}/{now}")
    @PreAuthorize("hasAuthority('API:ACS:DOWNLOAD-FILE')")
    @ResponseBody
    public AcsResponse send(Model model, @PathVariable("deviceId") String deviceId,
                     @PathVariable("fileId") String fileId,
                     @PathVariable("fileName") String fileName,
                     @PathVariable("now") Boolean now) throws UnsupportedEncodingException {
        AcsResponse result = acsApiClient.downloadFile(deviceId, now, fileId, fileName);
        if(result.httpResponseCode == 202 || result.httpResponseCode == 200) {
            result.mapResult.put(deviceId, String.valueOf(200));
        } else result.mapResult.put(deviceId, String.valueOf(404));
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"download",deviceId,loggingUserClient.getTaskId(result));
        return result;
    }

}
