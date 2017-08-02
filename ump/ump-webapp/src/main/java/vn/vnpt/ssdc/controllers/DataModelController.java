package vn.vnpt.ssdc.controllers;


import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vnpt.ssdc.api.client.ParameterDetailClient;
import vn.vnpt.ssdc.api.model.*;
import vn.vnpt.ssdc.datamodel.parser.DataModelParser;
import vn.vnpt.ssdc.datamodel.parser.DataModelParserFactory;
import vn.vnpt.ssdc.datamodel.parser.FileTypeNotSupportedException;
import vn.vnpt.ssdc.models.DataModelPaginator;
import vn.vnpt.ssdc.models.DeviceTypeDecorator;
import vn.vnpt.ssdc.models.DeviceTypeTree;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static vn.vnpt.ssdc.utils.Constants.NEW_PARAMETERS;
import static vn.vnpt.ssdc.utils.Constants.REMOVED_PARAMETERS;

/**
 * Created by vietnq on 11/2/16.
 */
@Controller
public class DataModelController extends BaseController {

    @Autowired
    protected ParameterDetailClient parameterDetailClient;

    private Logger logger = LoggerFactory.getLogger(DataModelController.class);

    private static final String DATA_MODEL_PAGE = "data_models/data_models";
    private static final String TAG_PAGE = "data_models/tags";
    private static final String PARAMETER_ADVANCED_VIEW_PAGE = "data_models/parameter_advanced_view";
    @Value("${apiEndpointUrl}")
    private String apiEndpointUrl;

    @Autowired
    private DataModelParserFactory parserFactory;

    @GetMapping("/data-models")
    @PreAuthorize("hasAuthority('API:DATA-MODEL:READ-LIST-DATA-MODELS')")
    public String index(Model model, @RequestParam HashMap<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to data model page","","");
        session.removeAttribute("listComboSearch");
        loadIndexPage(model, requestParams);
        return DATA_MODEL_PAGE;
    }

    @GetMapping("/data-models/search")
    @PreAuthorize("hasAuthority('API:DATA-MODEL:READ-LIST-DATA-MODELS')")
    public String search(Model model, @RequestParam HashMap<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"search on data model page","","");
        loadIndexPage(model, requestParams);
        return DATA_MODEL_PAGE;
    }

    private void loadIndexPage(Model model, HashMap<String, String> requestParams) {
//        addBaseRenderArgs(model, null);
        Map<String, String> deviceIndexParams = deviceModelIndexParams();
        DataModelPaginator dataModelPaginator = new DataModelPaginator();
        dataModelPaginator.deviceTypeClient = deviceTypeClient;
        dataModelPaginator.deviceTypeVersionClient = deviceTypeVersionClient;
        dataModelPaginator.indexParams = deviceIndexParams;
        dataModelPaginator.parseParam(requestParams);
        dataModelPaginator.loadResult(requestParams);
        //add render args

        model.addAttribute("paginator", dataModelPaginator);
        Object listComboSearch = session.getAttribute("listComboSearch");
        JsonObject response = new JsonObject();
        response = checkListComboSearchDataModel(listComboSearch);

        List<String> manufacturerlist = new ArrayList<String>();
        List<String> modelNamelist = new ArrayList<String>();
        if (response.size() > 0) {
            for (Map.Entry<String, JsonElement> entry : response.entrySet()) {
                manufacturerlist.add(entry.getKey());
                if (dataModelPaginator.searchManufacturerDataOld.equals(entry.getKey())) {
                    JsonArray asJsonArray = response.get(entry.getKey()).getAsJsonArray();
                    for (int i = 0; i < asJsonArray.size(); i++) {
                        modelNamelist.add(asJsonArray.get(i).toString().replace("\"", ""));

                    }
                }

            }
        }
        java.util.Collections.sort(manufacturerlist);
        if (modelNamelist.size() > 0) {
            modelNamelist.add(0, "All");
            java.util.Collections.sort(modelNamelist);
        }
        manufacturerlist.add(0, "All");
        model.addAttribute("manufacturerlist", manufacturerlist);
        model.addAttribute("modelNamelist", modelNamelist);
        model.addAttribute("manufacturerDataOld", dataModelPaginator.searchManufacturerDataOld);
        model.addAttribute("modelNameDataOld", dataModelPaginator.searchmodelNameDataOld);
        model.addAttribute("itemPerPage", requestParams.get("limit"));

        boolean resultWarning = false;
        if(session.getAttribute("listDeleteDevice") != null
                && !session.getAttribute("listDeleteDevice").equals("")){
            resultWarning = true;
            model.addAttribute("resultDeviceDeleteValue", "In-used Data Models "
                    + session.getAttribute("listDeleteDevice")
                    +" are not allowed to be deleted!");
        }
        model.addAttribute("resultWarningDeviceDelete", resultWarning);
        session.removeAttribute("listDeleteDevice");

    }



    @GetMapping("/data-models/export/{deviceTypeVersionId}")
    @PreAuthorize("hasAuthority('API:DATA-MODEL:EXPORT')")
    public void exportXml(@PathVariable Long deviceTypeVersionId, HttpServletResponse response) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"export xml data model","","");
        try {
            URL url = new URL(apiEndpointUrl + "/data-model/export/" + deviceTypeVersionId);
            response.setHeader("Content-disposition", "attachment;filename=data-model-" + deviceTypeVersionId + ".xml");

            //Set the mime type for the response
            response.setContentType("application/xml");

            InputStream is = url.openStream();

            BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
            int len;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) > 0) {
                outs.write(buf, 0, len);
            }
            outs.close();

        } catch (MalformedURLException e) {
            logger.error("Error ModelAndView.viewMain - MalformedURLException : " + e.toString() + " -- " + e.getStackTrace()[0].toString());
        } catch (IOException e) {
            logger.error("Error ModelAndView.viewMain - IOException : " + e.toString() + " -- " + e.getStackTrace()[0].toString());
        }
    }

    @GetMapping("/data-models/exportJson/{deviceTypeVersionId}")
    @PreAuthorize("hasAuthority('API:DATA-MODEL:EXPORT-JSON')")
    public void exportJson(@PathVariable Long deviceTypeVersionId, HttpServletResponse response)
    {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"export json data model","","");
        try {
            URL url = new URL(apiEndpointUrl+"/data-model/exportJson/"+deviceTypeVersionId);
            response.setHeader("Content-disposition", "attachment;filename=data-model-"+deviceTypeVersionId+".json");

            //Set the mime type for the response
            response.setContentType("application/json");

            InputStream is = url.openStream();

            BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
            int len;
            byte[] buf = new byte[1024];
            while ( (len = is.read(buf)) > 0 ) {
                outs.write(buf, 0, len);
            }
            outs.close();

        } catch (MalformedURLException e) {
            logger.error("Error ModelAndView.viewMain - MalformedURLException : " + e.toString() + " -- " + e.getStackTrace()[0].toString());
        } catch (IOException e) {
            logger.error("Error ModelAndView.viewMain - IOException : " + e.toString() + " -- " + e.getStackTrace()[0].toString());
        }
    }

    @PostMapping("/data-models")
    @PreAuthorize("hasAuthority('API:DATA-MODEL:CREATE')")
    public String create(Model model, @ModelAttribute DeviceTypeDecorator deviceTypeDecorator) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create a new device type","","");
        deviceTypeClient.create(deviceTypeDecorator.toDeviceType());
        return show(model, null);
    }

    @GetMapping("/data-models/{id}")
    public String show(Model model, @PathVariable Long id) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"view data model "+id.toString(),"","");
        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(id);
        model.addAttribute("deviceTypeVersionId", id);
        addBaseRenderArgs(model, deviceTypeVersion);
        return DATA_MODEL_PAGE;
    }

    @GetMapping("/data-models/{id}/list-tag-none-assign")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:TAGS')")
    @ResponseBody
    public Tag[] getListTagNoneAssign(@PathVariable Long id) {
        return deviceTypeVersionClient.findTagsForDeviceTypeVersion(id);
    }

    @GetMapping("/data-models/{id}/list-new-parameters")
    @ResponseBody
    public List<String> getListParameters(@PathVariable Long id) {
        Map<String, List<String>> map = this.getDiffWithPreviousVersion(id);
        return map.get(NEW_PARAMETERS);
    }

    @GetMapping("/data-models/{id}/list-removed-param")
    @ResponseBody
    public List<String> getListRemovedParameters(@PathVariable Long id) {
        Map<String, List<String>> map = this.getDiffWithPreviousVersion(id);
        return map.get(REMOVED_PARAMETERS);
    }

    @GetMapping("/data-models/{id}/get-list-diff-params")
    @ResponseBody
    public Map<String, List<String>> getListDiffParams(@PathVariable Long id) {
        return this.getDiffWithPreviousVersion(id);
    }

    @PostMapping("/data-models/upload")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:UPDATE')")
    public String uploadDataModel(Model model, @RequestParam Map<String, String> params,
                                  @RequestParam("dataModelFile") MultipartFile dataModelFile) {

        Long deviceTypeVersionId = Long.valueOf(params.get("deviceTypeVersionId"));
        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);
        if (dataModelFile != null) {
            try {
                DataModelParser parser = parserFactory.getParser(dataModelFile.getOriginalFilename());
                deviceTypeVersion.parameters = parser.parse(dataModelFile.getInputStream());
                deviceTypeVersion.dataModelFileName = dataModelFile.getOriginalFilename();
                deviceTypeVersionClient.update(deviceTypeVersion.id, deviceTypeVersion);

                //sort parameters
                deviceTypeVersion.parameters = new TreeMap<String, Parameter>(deviceTypeVersion.parameters);
            } catch (FileTypeNotSupportedException e) {
                logger.info("uploadDataModel ", e);
                model.addAttribute("errorMessage", "File type not supported");
            } catch (IOException e) {
                logger.info("uploadDataModel ", e);
                model.addAttribute("errorMessage", "Error when parsing data model file");
            }
        }
        addBaseRenderArgs(model, deviceTypeVersion);
        return "redirect:/data-models/" + deviceTypeVersionId + "#/overview-tab";
    }

    @GetMapping("/data-models/tag/{id}")
    @PreAuthorize("hasAuthority('API:TAG:READ-ONE-TAG')")
    @ResponseBody
    public Tag getTagDetail(@PathVariable Long id) {
        Tag tag = tagClient.get(id);
        return tag;
    }

    @PostMapping("/data-models/{id}/tags")
    @PreAuthorize("hasAuthority('API:TAG:CREATE')")
    public String createTag(Model model, @PathVariable("id") Long id,
                            @RequestParam String name,
                            @RequestParam String type,
                            @RequestParam String parentObject,
                            @RequestParam Map<String, String> params) {

        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(id);
        Tag tag = new Tag();
        tag.deviceTypeVersionId = id;
        tag.name = name;
        tag.parentObject = parentObject;
        tag = generateParameter(tag, params, deviceTypeVersion);
        tagClient.create(tag);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create a new profile","","");
        return "redirect:/data-models/" + deviceTypeVersion.id + "#/tags-tab";
    }

    @PostMapping("/data-models/{id}/tags/update")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    public String updateTag(Model model, @PathVariable("id") Long id,
                            @RequestParam Long tagId,
                            @RequestParam String name,
                            @RequestParam String type,
                            @RequestParam String parentObject,
                            @RequestParam Map<String, String> params) {

        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(id);
        Tag tag = tagClient.get(tagId);
        tag.name = name;
        tag.parentObject = parentObject;
        tag.parameters = new LinkedHashMap<String, Parameter>();
        tag = generateParameter(tag, params, deviceTypeVersion);
        tagClient.update(tagId, tag);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update profile","","");
        return "redirect:/data-models/" + deviceTypeVersion.id + "#/tags-tab";
    }

    private Tag generateParameter(Tag tag, Map<String, String> params, DeviceTypeVersion deviceTypeVersion) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("path_")) {
                String path = params.get(key);
                String fullPatch = "" + tag.parentObject + path;
                if (!ObjectUtils.empty(path) && deviceTypeVersion.parameters.containsKey(fullPatch)) {
                    String index = key.split("path_")[1];
                    Parameter parameter = deviceTypeVersion.parameters.get(fullPatch);
                    if (ObjectUtils.empty(parameter)) {
                        parameter = new Parameter();
                    }
                    parameter.path = path;
                    parameter.value = params.get("value_" + index);
                    parameter.shortName = params.get("shortName_" + index);
                    tag.parameters.put(path, parameter);
                }
            }
        }
        return tag;
    }

    private void addBaseRenderArgs(Model model, DeviceTypeVersion currentDeviceTypeVersion) {
        model.addAttribute("currentActiveHeader", HEADER_DATA_MODELS);
        model.addAttribute("deviceTypeDecorator", new DeviceTypeDecorator());
        DeviceTypeTree tree = new DeviceTypeTree(deviceTypeClient, true);
        tree.load();
        model.addAttribute("tree", tree);
        model.addAttribute("treeTitle", "DATA MODELS");
        model.addAttribute("showAddVersion", "false");
        model.addAttribute("showAddDeviceType", "false");

        if (ObjectUtils.empty(currentDeviceTypeVersion)) {
            currentDeviceTypeVersion = tree.firstDeviceTypeVersion;
        }

        if (!ObjectUtils.empty(currentDeviceTypeVersion)) {
            DeviceType deviceType = deviceTypeClient.get(currentDeviceTypeVersion.deviceTypeId);
            model.addAttribute("deviceType", deviceType);
            model.addAttribute("currentDeviceTypeVersion", currentDeviceTypeVersion);
            Tag[] tags = deviceTypeVersionClient.findTagsForDeviceTypeVersion(currentDeviceTypeVersion.id);
            model.addAttribute("noTags", tags == null || tags.length == 0);
            model.addAttribute("tags", tags);
        }
        //add parameter list to template to have auto complete
        String params = "";
        String objectParams = "";
        if (!ObjectUtils.empty(currentDeviceTypeVersion) && !ObjectUtils.empty(currentDeviceTypeVersion.parameters)) {
            Map<String, Parameter> parameters = new TreeMap<String, Parameter>(currentDeviceTypeVersion.parameters);
            params = String.join(",", parameters.keySet());
            for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
                Parameter paramObj = parameters.get(entry.getKey());
                if (Objects.equals(paramObj.dataType, "object")
                        || (Objects.equals(paramObj.dataType, "string")
                        && Objects.equals(paramObj.path.substring(paramObj.path.length() - 1), "."))) {
                    objectParams += paramObj.path + ",";
                }
            }
        }
        model.addAttribute("params", params);
        model.addAttribute("objectParams", objectParams);


        model.addAttribute("currentActiveHeader", HEADER_DEVICES);
        //add lisColumn
        LinkedHashMap<String, String> txtProps = new LinkedHashMap<>();
        List<String> listColumn = getListColumSearch();
        for (int i = 0; i < listColumn.size(); i++) {
            txtProps.put(listColumn.get(i), "");
        }
        model.addAttribute("txtProps", txtProps);
    }

    private List<String> getListColumSearch() {
        List<String> ds = new ArrayList<>();
        if (LIST_COLUMN_DATA_MODEL != null && !LIST_COLUMN_DATA_MODEL.isEmpty()) {
            String[] column = LIST_COLUMN_DATA_MODEL.split(",");
            for (int i = 0; i < column.length; i++) {
                ds.add(column[i]);
            }
        }
        return ds;
    }

    @PostMapping("/data-models/{id}/assign")
    @PreAuthorize("hasAuthority('API:TAG:CREATE')")
    @ResponseBody
    public Tag assign(@RequestBody Tag tag) {
        tag.id = null;
        tag.assigned = 1;
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create a new tag","","");
        return this.tagClient.create(tag);
    }

    @PostMapping("/data-models/{id}/assign/update")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Tag updateAssign(@RequestBody Tag tag) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update tag","","");
        tagClient.update(tag.id, tag);
        return tag;
    }

    @DeleteMapping("/data-models/{id}/assign/{tagId}")
    @PreAuthorize("hasAuthority('API:TAG:DELETE')")
    @ResponseBody
    public void deleteAssign(@PathVariable("tagId") Long tagId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete tag","","");
        tagClient.delete(tagId);
    }

    private Map<String, List<String>> getDiffWithPreviousVersion(Long deviceTypeVersionId) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put(NEW_PARAMETERS, new ArrayList<String>());
        map.put(REMOVED_PARAMETERS, new ArrayList<String>());

        DeviceTypeVersion current = deviceTypeVersionClient.get(deviceTypeVersionId);
        DeviceTypeVersion prev = deviceTypeVersionClient.prev(deviceTypeVersionId);

        if (!ObjectUtils.empty(current) && !ObjectUtils.empty(prev) && !ObjectUtils.empty(current.parameters) && !ObjectUtils.empty(prev.parameters)) {
            current.parameters.keySet().stream().filter(parameter -> !prev.parameters.containsKey(parameter)).forEach(parameter -> {
                map.get(NEW_PARAMETERS).add(parameter);
            });

            prev.parameters.keySet().stream().filter(parameter -> !current.parameters.containsKey(parameter)).forEach(parameter -> {
                map.get(REMOVED_PARAMETERS).add(parameter);
            });
        }
        return map;
    }

    @GetMapping("data-models/{deviceTypeVersionId}/profile")
    public String getListProfile(Model model, @PathVariable("deviceTypeVersionId") Long deviceTypeVersionId) {

        // Get list tags
        Tag[] tags = deviceTypeVersionClient.findTagsForDeviceTypeVersion(deviceTypeVersionId);
        Tag tag = new Tag();
        if (tags.length > 0) {
            tag = tags[0];
            return "redirect:/data-models/" + deviceTypeVersionId + "/profile/" + tag.id;
        } else {
            model.addAttribute("noTag", true);
            model.addAttribute("noParam", true);
            return TAG_PAGE;
        }
    }

    @GetMapping("data-models/{deviceTypeVersionId}/profile/{tagId}")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:READ-ONE-DEVICE-TYPE-VERSION')")
    public String getProfile(Model model,
                             @PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                             @PathVariable("tagId") Long tagId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"view profile "+tagId.toString(),"","");
        // Get list profile
        Tag[] tags = deviceTypeVersionClient.findTagsForDeviceTypeVersion(deviceTypeVersionId);
//        sortProfile(tags);
        Boolean isOthersTag = false;
        Tag tag = null;
        try {
            tag = tagClient.get(tagId);
            isOthersTag = "Others".equals(tag.name);
        } catch (Exception e) {
            return "redirect:/data-models/" + deviceTypeVersionId + "/profile";
        }

        ParameterDetail[] parameterDetails = parameterDetailClient.findByDeviceTypeVersion(deviceTypeVersionId);
        String listPath = "";
        for (ParameterDetail parameterDetail : parameterDetails) {
            if (!"object".equals(parameterDetail.dataType)) {
                listPath += parameterDetail.path + ",";
            }
        }
        listPath = listPath.length() > 0 ? listPath.substring(0, listPath.length() - 1) : listPath;

        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);
        DeviceType deviceType = deviceTypeClient.get(deviceTypeVersion.deviceTypeId);

        model.addAttribute("tags", tags);
        model.addAttribute("tagId", tagId);
        model.addAttribute("isOthersTag", isOthersTag);
        model.addAttribute("deviceTypeVersionId", deviceTypeVersionId);
        model.addAttribute("noTag", false);
        model.addAttribute("listPath", listPath);
        model.addAttribute("deviceTypeVersion", deviceTypeVersion);
        model.addAttribute("deviceType", deviceType);

        return TAG_PAGE;
    }

    @GetMapping("data-models/profile/{tagId}/get-list-parameters")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:READ-ONE-DEVICE-TYPE-VERSION')")
    @ResponseBody
    public Set<Parameter> getListParametersByTagId(@PathVariable("tagId") Long tagId) {
        Set<Parameter> parameters = new LinkedHashSet<>();
        if(tagId > 0) {
            Tag tag = tagClient.get(tagId);
            for (String path : new TreeSet<>(tag.parameters.keySet())) {

                Parameter parameter = tag.parameters.get(path);
                if (!ObjectUtils.empty(parameter) && !"object".equals(parameter.dataType)) {
                    if ("dateTime".equals(parameter.dataType)) {
                        String dateTime = ObjectUtils.empty(parameter.value) ? parameter.defaultValue : parameter.value;
                        dateTime = StringUtils.toSampleDate(dateTime);
                        parameter.value = dateTime;
                        parameter.defaultValue = dateTime;
                    }
                    parameters.add(parameter);
                }
            }
        }

        return parameters;
    }

    @GetMapping("data-models/profile/{tagId}/get-list-parent-objects")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:READ-ONE-DEVICE-TYPE-VERSION')")
    @ResponseBody
    public Map<String, Set<String>> getListParentObject(@PathVariable("tagId") Long tagId,
                                                        @RequestParam(value = "parentObject", defaultValue = "") String parentObjectSearch) {
        Set<String> parentObjects = new TreeSet<>();
        Set<String> parentObjectResultSearch = new TreeSet<>();
        Tag tag = tagClient.get(tagId);
        for (String path : new TreeSet<>(tag.parameters.keySet())) {
            Parameter parameter = tag.parameters.get(path);
            if (!ObjectUtils.empty(parameter) && !"object".equals(parameter.dataType)) {
                parentObjects.add(parameter.path.substring(0, parameter.path.lastIndexOf(".")));

                if (!"".equals(parentObjectSearch) && parameter.path.contains(parentObjectSearch)) {
                    parentObjectResultSearch.add(parameter.path.substring(0, parameter.path.lastIndexOf(".")));
                }
            }
        }

        // Remove parent object non instance
        Set<String> resultParentObjects = new TreeSet<>(parentObjects);
        for (String parentObject : parentObjects) {
            String removeParentObject = "";
            for (String parentObject2 : parentObjects) {
                if(parentObject2.contains(parentObject) && !parentObject.equals(parentObject2)) {
                    removeParentObject = parentObject;
                }
            }
            resultParentObjects.remove(removeParentObject);
        }

        Map<String, Set<String>> result = new LinkedHashMap<>();
        result.put("parentObjects", resultParentObjects);
        result.put("parentObjectResultSearch", parentObjectResultSearch);

        return result;
    }

    @GetMapping("data-models/{deviceTypeVersionId}/profile/get-list-parameters")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:READ-ONE-DEVICE-TYPE-VERSION')")
    @ResponseBody
    public Set<Parameter> getListParametersByDeviceTypeVersionId(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId) {
        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);
        Set<Parameter> parameters = new LinkedHashSet<>();
        for (String path : new TreeSet<>(deviceTypeVersion.parameters.keySet())) {
            if (!ObjectUtils.empty(deviceTypeVersion.parameters.get(path)) && !"object".equals(deviceTypeVersion.parameters.get(path).dataType)) {

                Parameter parameter = deviceTypeVersion.parameters.get(path);
                if (!ObjectUtils.empty(parameter) && !"object".equals(parameter.dataType)) {
                    if ("dateTime".equals(parameter.dataType)) {
                        String dateTime = ObjectUtils.empty(parameter.value) ? parameter.defaultValue : parameter.value;
                        dateTime = StringUtils.toSampleDate(dateTime);
                        parameter.value = dateTime;
                        parameter.defaultValue = dateTime;
                    }
                    parameters.add(parameter);
                }
            }
        }

        return parameters;
    }

    @GetMapping("data-models/{deviceTypeVersionId}/profile/existed")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:READ-ONE-DEVICE-TYPE-VERSION')")
    @ResponseBody
    public Boolean profileExisted(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                  @RequestParam("name") String name) {
        Boolean check = false;
        Tag[] tags = deviceTypeVersionClient.findTagsForDeviceTypeVersion(deviceTypeVersionId);
        for (Tag tag : tags) {
            if (tag.name.toLowerCase().equals(name.toLowerCase())) {
                check = true;
                break;
            }
        }
        return check;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/profile/{tagId}/update")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:READ-ONE-DEVICE-TYPE-VERSION')")
    public String updateProfile(@PathVariable("tagId") Long tagId,
                                @PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update profile "+tagId,"","");
        ParameterDetail[] parameterDetails = parameterDetailClient.findByDeviceTypeVersion(deviceTypeVersionId);

        for (String key : params.keySet()) {
            if(key.startsWith("path_")) {
                String path = key.substring("path_".length(), key.length());
                String value = params.get(key);

                // Convert data if data type is date time
                String dataType = params.containsKey("dataType_" + path) ? params.get("dataType_" + path) : "";
                if("dateTime".equals(dataType)) {
                    value = StringUtils.toZoneDateTime(value);
                }

                // Update parameter_details
                for (ParameterDetail parameterDetail : parameterDetails) {
                    if (path.equals(parameterDetail.path)) {
                        parameterDetail.profile = new HashSet<>();
                        parameterDetail.profile.add(String.valueOf(tagId));
                        parameterDetailClient.update(parameterDetail.id, parameterDetail);
                        break;
                    }
                }

                // Update device_type_version
                DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);
                if (!ObjectUtils.empty(deviceTypeVersion.parameters.get(path))) {
                    deviceTypeVersion.parameters.get(path).value = value;
                    deviceTypeVersionClient.update(deviceTypeVersionId, deviceTypeVersion);
                }

                // Update tags
                Tag tag = tagClient.get(tagId);
                if (!ObjectUtils.empty(tag.parameters.get(path))) {
                    tag.parameters.get(path).value = value;
                    tagClient.update(tagId, tag);
                }
            }
        }

        String url = params.containsKey("currentUrl") ? params.get("currentUrl")
                : "/data-models/" + deviceTypeVersionId + "/profile/" + tagId;
        return "redirect:" + url;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/profile/create")
    @PreAuthorize("hasAuthority('API:TAG:CREATE')")
    public String createProfile(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                @RequestParam String name) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create new profile","","");
        Tag tag = new Tag();
        tag.deviceTypeVersionId = deviceTypeVersionId;
        tag.name = name.trim();
        tag = tagClient.create(tag);

        return "redirect:/data-models/" + deviceTypeVersionId + "/profile/" + tag.id;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/profile/{tagId}/delete")
    @PreAuthorize("hasAuthority('API:TAG:DELETE')")
    @ResponseBody
    public Long deleteTag(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                          @PathVariable("tagId") Long tagId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete tag "+tagId,"","");
        Tag tag = tagClient.get(tagId);
        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);
        ParameterDetail[] parameterDetails = parameterDetailClient.findByDeviceTypeVersion(deviceTypeVersionId);
        for (String path : tag.parameters.keySet()) {

            // Update device type version
            if (!ObjectUtils.empty(path) && !ObjectUtils.empty(deviceTypeVersion.parameters.get(path))) {
                deviceTypeVersion.parameters.remove(path);
            }

            // Update parameter_details
            for (ParameterDetail parameterDetail : parameterDetails) {
                if (path.equals(parameterDetail.path)) {
                    parameterDetail.profile = new HashSet<>();
                    parameterDetailClient.update(parameterDetail.id, parameterDetail);
                    break;
                }
            }
        }
        tagClient.delete(tagId);
        deviceTypeVersionClient.update(deviceTypeVersionId, deviceTypeVersion);

        return tagId;
    }

    @PostMapping("data-models/profile/{tagId}/synchronize")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Long synchronizeProfile(@PathVariable("tagId") Long tagId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"synchronize/unsynchronize profile "+tagId,"","");
        Tag tag = tagClient.get(tagId);
        tag.synchronize = ObjectUtils.empty(tag.synchronize) || tag.synchronize == 0 ? 1 : 0;
        tagClient.update(tagId, tag);

        return tagId;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/profile/{tagId}/add-parameter")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Long addParameterToProfile(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                      @PathVariable("tagId") Long tagId,
                                      @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update profile "+tagId+"(add parameter)","","");
        ParameterDetail[] parameterDetails = parameterDetailClient.findByDeviceTypeVersion(deviceTypeVersionId);
        Map<String, ParameterDetail> parameterDetailsMap = new HashMap<String, ParameterDetail>();
        for (ParameterDetail pd : parameterDetails) {
            parameterDetailsMap.put(pd.path, pd);
        }

        Tag tag = tagClient.get(tagId);
        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);

        for (String key : params.keySet()) {
            if (key.startsWith("paths")) {
                String path = params.get(key);
                if (!ObjectUtils.empty(path) && parameterDetailsMap.containsKey(path)) {

                    ParameterDetail parameterDetail = parameterDetailsMap.get(path);

                    // Remove form other profile
                    if(!parameterDetail.profile.contains(String.valueOf(tagId))) {
                        for (String otherProfileId : parameterDetail.profile) {
                            Tag tagRemoveParameter = tagClient.get(Long.valueOf(otherProfileId));
                            tagRemoveParameter.parameters.remove(path);
                            tagClient.update(tagRemoveParameter.id, tagRemoveParameter);
                        }
                    }

                    // Add parameter to profile
                    Parameter parameter = new Parameter();
                    parameter.path = parameterDetail.path;
                    parameter.shortName = parameterDetail.shortName;
                    parameter.dataType = parameterDetail.dataType;
                    parameter.defaultValue = parameterDetail.defaultValue;
                    parameter.rule = parameterDetail.rule;
//                parameter.inputType = parameterDetail.inputType;
//                parameter.useSubscriberData = parameterDetail.useSubscriberData;
                    parameter.tr069Name = parameterDetail.tr069Name;
                    parameter.tr069ParentObject = parameterDetail.tr069ParentObject;
                    parameter.access = parameterDetail.access;
                    tag.parameters.put(path, parameter);

                    // Update device_type_version
                    if (ObjectUtils.empty(deviceTypeVersion.parameters.get(parameter.path))) {
                        deviceTypeVersion.parameters.put(parameter.path, parameter);
                    }

                    // Update parameter_details
                    parameterDetail.profile = new HashSet<>();
                    parameterDetail.profile.add(String.valueOf(tagId));
                    parameterDetailClient.update(parameterDetail.id, parameterDetail);
                }
            }
        }

        tagClient.update(tagId, tag);
        deviceTypeVersionClient.update(deviceTypeVersionId, deviceTypeVersion);

        return tagId;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/profile/{tagId}/move-parameter")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Long moveParameterToOtherProfile(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                            @PathVariable("tagId") Long tagId,
                                            @RequestParam("tagIdMove") Long tagIdMove,
                                            @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update profile "+tagId+"(move parameter)","","");
        Tag tag = tagClient.get(tagId);
        Tag tagMove = tagClient.get(tagIdMove);

        for (String key : params.keySet()) {
            if (key.startsWith("paths")) {
                String path = params.get(key);
                if (!ObjectUtils.empty(path) && !ObjectUtils.empty(tag.parameters.get(path))) {

                    Parameter parameter = tag.parameters.get(path);
                    Parameter parameterMove = new Parameter();
                    parameterMove.path = parameter.path;
                    parameterMove.shortName = parameter.shortName;
                    parameterMove.dataType = parameter.dataType;
                    parameterMove.value = parameter.value;
                    parameterMove.defaultValue = parameter.defaultValue;
                    parameterMove.rule = parameter.rule;
                    parameterMove.inputType = parameter.inputType;
                    parameterMove.useSubscriberData = parameter.useSubscriberData;
                    parameterMove.access = parameter.access;
                    parameterMove.tr069Name = parameter.tr069Name;
                    parameterMove.tr069ParentObject = parameter.tr069ParentObject;
                    tagMove.parameters.put(path, parameterMove);
                    tag.parameters.remove(path);
                }
            }
        }

        tagClient.update(tagId, tag);
        tagClient.update(tagIdMove, tagMove);

        return tagId;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/profile/{tagId}/delete-parameter")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Long deleteParameterFromProfile(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                           @PathVariable("tagId") Long tagId,
                                           @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update profile "+tagId+"(delete parameter)","","");
        Tag tag = tagClient.get(tagId);
        Tag tagOthers = tagClient.getProfileOthers(deviceTypeVersionId);

        for (String key : params.keySet()) {
            if (key.startsWith("paths")) {
                String path = params.get(key);
                if (!ObjectUtils.empty(path) && !ObjectUtils.empty(tag.parameters.get(path))) {

                    Parameter parameter = tag.parameters.get(path);
                    Parameter parameterMove = new Parameter();
                    parameterMove.path = parameter.path;
                    parameterMove.shortName = parameter.shortName;
                    parameterMove.dataType = parameter.dataType;
                    parameterMove.value = parameter.value;
                    parameterMove.defaultValue = parameter.defaultValue;
                    parameterMove.rule = parameter.rule;
                    parameterMove.inputType = parameter.inputType;
                    parameterMove.useSubscriberData = parameter.useSubscriberData;
                    parameterMove.access = parameter.access;
                    parameterMove.tr069Name = parameter.tr069Name;
                    parameterMove.tr069ParentObject = parameter.tr069ParentObject;
                    tagOthers.parameters.put(path, parameterMove);
                    tag.parameters.remove(path);
                }
            }
        }

        tagClient.update(tagId, tag);
        tagClient.update(tagOthers.id, tagOthers);


        return tagId;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/profile/{tagId}/move-parameter-object")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Long moveParameterObjectToOtherProfile(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                                  @PathVariable("tagId") Long tagId,
                                                  @RequestParam("tagIdMove") Long tagIdMove,
                                                  @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update profile "+tagId+"(move object)","","");
        Tag tag = tagClient.get(tagId);
        Tag tagMove = tagClient.get(tagIdMove);
        Map<String, Parameter> parameters = new HashMap<>(tag.parameters);

        for (String keyParam : params.keySet()) {
            if (keyParam.startsWith("paths")) {
                String pathObject = params.get(keyParam);
                for (String path : parameters.keySet()) {
                    if (!ObjectUtils.empty(path) && path.indexOf(pathObject) == 0) {
                        Parameter parameter = tag.parameters.get(path);
                        Parameter parameterMove = new Parameter();
                        parameterMove.path = parameter.path;
                        parameterMove.shortName = parameter.shortName;
                        parameterMove.dataType = parameter.dataType;
                        parameterMove.value = parameter.value;
                        parameterMove.defaultValue = parameter.defaultValue;
                        parameterMove.rule = parameter.rule;
                        parameterMove.inputType = parameter.inputType;
                        parameterMove.useSubscriberData = parameter.useSubscriberData;
                        parameterMove.access = parameter.access;
                        parameterMove.tr069Name = parameter.tr069Name;
                        parameterMove.tr069ParentObject = parameter.tr069ParentObject;
                        tagMove.parameters.put(path, parameterMove);
                        if (!ObjectUtils.empty(tag.parameters.get(path))) {
                            tag.parameters.remove(path);
                        }
                    }
                }
            }
        }

        tagClient.update(tagId, tag);
        tagClient.update(tagIdMove, tagMove);

        return tagId;
    }

    @GetMapping("data-models/{deviceTypeVersionId}/parameter-advanced-view")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:READ-ONE-DEVICE-TYPE-VERSION')")
    public String getParameterAdvancedView(Model model,
                                           @PathVariable("deviceTypeVersionId") Long deviceTypeVersionId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"advanced view data model "+deviceTypeVersionId.toString(),"","");
        DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);
        DeviceType deviceType = deviceTypeClient.get(deviceTypeVersion.deviceTypeId);

        model.addAttribute("deviceTypeVersionId", deviceTypeVersionId);
        model.addAttribute("deviceTypeVersion", deviceTypeVersion);
        model.addAttribute("deviceType", deviceType);

        return PARAMETER_ADVANCED_VIEW_PAGE;
    }

    @PostMapping("data-models/{deviceTypeVersionId}/parameter-advanced-view/update")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:UPDATE')")
    public String updateParameter(@PathVariable("deviceTypeVersionId") Long deviceTypeVersionId,
                                  @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update data model "+deviceTypeVersionId.toString(),"","");
        ParameterDetail[] parameterDetails = parameterDetailClient.findByDeviceTypeVersion(deviceTypeVersionId);
        for (String key: params.keySet()) {

            if(key.startsWith("path_")) {
                String path = key.substring("path_".length(), key.length());
                String value = params.get(key);

                // Convert data if data type is date time
                String dataType = params.containsKey("dataType_" + path) ? params.get("dataType_" + path) : "";
                if("dateTime".equals(dataType)) {
                    value = StringUtils.toZoneDateTime(value);
                }

                // Update parameter_details
                for (ParameterDetail parameterDetail : parameterDetails) {
                    if (path.equals(parameterDetail.path)) {
                        parameterDetail.profile = new HashSet<>();
                        parameterDetailClient.update(parameterDetail.id, parameterDetail);

                        // Update tags
                        for (String tagId : parameterDetail.profile) {
                            Tag tag = tagClient.get(Long.valueOf(tagId));
                            if (!ObjectUtils.empty(tag) && !ObjectUtils.empty(tag.parameters.get(path))) {
                                tag.parameters.get(path).value = value;
                                tagClient.update(Long.valueOf(tagId), tag);
                            }
                        }

                        break;
                    }
                }

                // Update device_type_version
                DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);
                if (!ObjectUtils.empty(deviceTypeVersion.parameters.get(path))) {
                    deviceTypeVersion.parameters.get(path).value = value;
                    deviceTypeVersionClient.update(deviceTypeVersionId, deviceTypeVersion);
                }
            }
        }

        String url = params.containsKey("currentUrl") ? params.get("currentUrl") : "/data-models/" + deviceTypeVersionId + "/parameter-advanced-view";
        return "redirect:" + url;
    }


    private void sortProfile(Tag[] tags) {
        Arrays.sort(tags, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    private void sortParameterDetail(ParameterDetail[] parameterDetails) {
        Arrays.sort(parameterDetails, new Comparator<ParameterDetail>() {
            @Override
            public int compare(ParameterDetail o1, ParameterDetail o2) {
                return o1.path.compareTo(o2.path);
            }
        });
    }

    @PostMapping("/data-models/delete")
    @PreAuthorize("hasAuthority('API:DEVICE-TYPE-VERSION:DELETE')")
    @ResponseBody
    public String delete(Model model, @RequestParam Map<String, String> params) {
        String result = "";
        DataModelPaginator dataModelPaginator = new DataModelPaginator();
        if (params.containsKey("deleteIds") && params.containsKey("deleteParams")) {
            String deleteIds = params.get("deleteIds");
            String deleteParams = params.get("deleteParams");
            deleteIds = deleteIds.substring(0, deleteIds.length() - 1);
            deleteParams = deleteParams.substring(0, deleteParams.length() - 1);
            String[] splitDeleteIds = deleteIds.split(",");
            String[] splitDeleteParams = deleteParams.split(",");
            Map<String, String> stringStringMap = new HashMap<>();
            for (int i = 0; i < splitDeleteIds.length; i++) {
                stringStringMap = dataModelPaginator.parseParamDelete(splitDeleteParams[i]);
                AcsResponse devices = acsApiClient.findDevices(stringStringMap);
                if (devices.httpResponseCode == 200) {
                    JsonArray array = new Gson().fromJson(devices.body, JsonArray.class);
                    if (array.toString().equals("[]")) {
                        deviceTypeVersionClient.delete(Long.parseLong(splitDeleteIds[i]));
                        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete data model "+Long.parseLong(splitDeleteIds[i]),"","");
                    } else {
                        String[] split = splitDeleteParams[i].split("#");
                        result += "," + split[1];
                    }
                }
            }

        }

        if (!result.equals("")) {
            result = result.substring(1);
        }
        session.setAttribute("listDeleteDevice",result);
        dataModelPaginator.parseParamAfterDelete();
        return result;
    }

}