package vn.vnpt.ssdc.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vnpt.ssdc.api.client.FileManagementClient;
import vn.vnpt.ssdc.api.client.FileUploadClient;
import vn.vnpt.ssdc.api.model.DeviceType;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.models.File;
import vn.vnpt.ssdc.models.FileManagementPaginator;
import vn.vnpt.ssdc.utils.ObjectUtils;

import java.util.*;

/**
 * Created by Lamborgini on 3/6/2017.
 */
@Controller
public class FileManagementController extends BaseController {
    private static final String FILE_MANAGEMENT_PAGE = "file_management/file_management";

    private static final Logger logger = LoggerFactory.getLogger(FileManagementController.class);

    @Autowired
    private FileManagementClient fileManagementClient;

    @Autowired
    private FileUploadClient fileUploadClient;

    @GetMapping("/file_management")
    @PreAuthorize("hasAuthority('API:FILE:SEARCH-FILE')")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to file management page","","");
        session.removeAttribute("listComboSearch");
        loadDataIndex(model, requestParams, "index");
        return FILE_MANAGEMENT_PAGE;
    }

    private void loadDataIndex(Model model, Map<String, String> requestParams, String mode) {
        Map<String, String> fileManagementIndexParams = fileManagementIndexParams();
        FileManagementPaginator fileManagementPaginator = new FileManagementPaginator();
        fileManagementPaginator.fileManagementClient = fileManagementClient;
        fileManagementPaginator.indexParams = fileManagementIndexParams;
        if (mode.equals("index")) {
            fileManagementPaginator.parseParam((HashMap<String, String>) requestParams);
        } else {
            fileManagementPaginator.parseParamSearch((HashMap<String, String>) requestParams, fileManagementIndexParams, fileIndexParamsReverse());
        }

        fileManagementPaginator.loadResult(requestParams);

        model.addAttribute("paginator", fileManagementPaginator);
        loadDataSearch(model, requestParams);
    }

    @GetMapping("/file_management/search")
    @PreAuthorize("hasAuthority('API:FILE:SEARCH-FILE')")
    public String search(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"search on file management page","","");
        loadDataIndex(model, requestParams, "search");
        return FILE_MANAGEMENT_PAGE;
    }

    @PostMapping("/file_management/delete/")
    @PreAuthorize("hasAuthority('API:FILE:DELETE-FILE')")
    @ResponseBody
    public String delete(Model model, @RequestParam Map<String, String> params) {
        StringBuilder result = new StringBuilder("");
        if (params.keySet().contains("deleteIds")) {
            String[] paramId = params.get("deleteIds").substring(0, params.get("deleteIds").length() - 1).split(",");
            String[] paramName = params.get("deleteNames").substring(0, params.get("deleteNames").length() - 1).split(",");
            if (paramId.length > 0) {
                for (int i = 0; i < paramId.length; i++) {
                    logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete file "+paramName[i],"","");
                    result.append("," + fileManagementClient.deleteFile(paramId[i], paramName[i]));
                }
            }
            if (result.equals("null")) {
                result.deleteCharAt(0);
            }
        }
        return result.toString();
    }

    @PostMapping("/file_management/addNewFile")
    @PreAuthorize("hasAuthority('API:FILE:CREATE-FILE')")
    @ResponseBody
    public Boolean addNewFile(Model model, @RequestParam Map<String, String> params,
                              @RequestParam("firmwareFile") MultipartFile firmwareFile) {
        if (firmwareFile != null) {
            DeviceType[] deviceTypes = deviceTypeClient.findByManufacturerAndModelName(
                    params.get("manufacturer"), params.get("modelName"));
            if (deviceTypes != null && deviceTypes.length > 0) {
                String oui = deviceTypes[0].getOui();
                String productClass = deviceTypes[0].getProductClass();
                String firmwareFileName = oui + "-" + productClass + "-" + params.get("version") + "-" + params.get("fileType");
                String name = firmwareFileName.replaceAll("\\s+", "");
                Boolean uploaded = false;
                if (params.get("idEditFile") != null && !params.get("idEditFile").isEmpty()) {
                    logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update file "+params.get("idEditFile"),"","");
                    if (firmwareFile.getOriginalFilename().isEmpty()) {
                        uploaded = fileUploadClient.updateFile(params.get("idEditFile")
                                , params.get("manufacturer"), params.get("modelName"), params.get("fileType"), params.get("version")
                                , oui, productClass);
                    } else {
                        uploaded = fileUploadClient.uploadFile(firmwareFile, params.get("idEditFile")
                                , params.get("manufacturer"), params.get("modelName"), params.get("fileType"), params.get("version")
                                , oui, productClass, firmwareFile.getOriginalFilename());
                    }

                } else {
                    logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"add a new file","","");
                    uploaded = fileUploadClient.uploadFile(firmwareFile, name
                            , params.get("manufacturer"), params.get("modelName"), params.get("fileType"), params.get("version")
                            , oui, productClass, firmwareFile.getOriginalFilename());
                }
                if (uploaded) {
                    return true;
                }
            }
        }

        return false;
    }

    @PostMapping("/file_management/existed")
    @ResponseBody
    public Boolean firmwareExisted(Model model, @RequestParam Map<String, String> params) {

        Boolean isExisted = false;
        DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findByManufacturerAndModelName(
                params.get("manufacturer"), params.get("modelName"));
        if (!ObjectUtils.empty(deviceTypeVersions)) {
            List<File> fileList = new ArrayList<File>();
            for (int i = 0; i < deviceTypeVersions.length; i++) {
                File file = new File();
                file.setOui(deviceTypeVersions[i].oui);
                file.setProductClass(deviceTypeVersions[i].productClass);
                file.setVersion(params.get("version"));
                fileList.add(file);
            }
            isExisted = fileManagementClient.checkByVersion(fileList);
        }


        return isExisted;
    }

    private void loadDataSearch(Model model, Map<String, String> requestParams) {

        FileManagementPaginator fileManagementPaginator = new FileManagementPaginator();
        if (requestParams.size() > 0) {
            if (requestParams.get(FileManagementPaginator.PAGE_MANUFACTURER) != null) {
                fileManagementPaginator.searchManufacturerDataOld = requestParams.get(FileManagementPaginator.PAGE_MANUFACTURER);
            }
            if (requestParams.get(FileManagementPaginator.PAGE_MODEL_NAME) != null) {
                fileManagementPaginator.searchModelNameDataOld = requestParams.get(FileManagementPaginator.PAGE_MODEL_NAME);
            }
            if (requestParams.get(FileManagementPaginator.PAGE_FILE_TYPE) != null
                    && !requestParams.get(FileManagementPaginator.PAGE_FILE_TYPE).equals("")) {
                fileManagementPaginator.searcFileTypeDataOld = String.valueOf(requestParams.get(FileManagementPaginator.PAGE_FILE_TYPE));
            }
        }

        Object listComboSearch = session.getAttribute("listComboSearch");
        JsonObject response = new JsonObject();
        response = checkListComboSearchDataModel(listComboSearch);

        List<String> manufacturerlist = new ArrayList<String>();
        List<String> modelNamelist = new ArrayList<String>();
        if (response.size() > 0) {
            for (Map.Entry<String, JsonElement> entry : response.entrySet()) {
                manufacturerlist.add(entry.getKey());
                if (fileManagementPaginator.searchManufacturerDataOld.equals(entry.getKey())) {
                    JsonArray asJsonArray = response.get(entry.getKey()).getAsJsonArray();
                    for (int i = 0; i < asJsonArray.size(); i++) {
                        modelNamelist.add(asJsonArray.get(i).toString().replace("\"", ""));
                    }
                }

            }
        }
        java.util.Collections.sort(manufacturerlist);
        if (modelNamelist.size() > 0) {
            java.util.Collections.sort(modelNamelist);
        }

        model.addAttribute("manufacturerlist", manufacturerlist);
        model.addAttribute("modelNamelist", modelNamelist);
        model.addAttribute("addNewFileParam", response.toString());
        model.addAttribute("manufacturerDataOld", fileManagementPaginator.searchManufacturerDataOld);
        model.addAttribute("modelNameDataOld", fileManagementPaginator.searchModelNameDataOld);
        model.addAttribute("fileTypeDataOld", fileManagementPaginator.searcFileTypeDataOld);
        model.addAttribute("itemPerPage", requestParams.size() == 0 ? "20" : requestParams.get("limit"));
    }
}
