package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.*;
import vn.vnpt.ssdc.api.model.DeviceTypeVersion;
import vn.vnpt.ssdc.api.model.Parameter;
import vn.vnpt.ssdc.api.model.Provisioning;
import vn.vnpt.ssdc.api.model.SubscriberTemplate;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.DeviceTypeVersionPaginator;
import vn.vnpt.ssdc.models.ProvisioningPaginator;
import vn.vnpt.ssdc.models.TagPaginator;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Controller
public class ProvisioningController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProvisioningController.class);

    private static final String PROVISIONING_PAGE = "provisioning/provisioning";
    private static final String PROVISIONING_VIEW_BY_TAG_PAGE = "provisioning/provisioning_view_by_tag";

    private static final String VIEW_BY_DEVICE = "device";
    private static final String VIEW_BY_TAG = "tag";

    @Autowired
    SubscriberDeviceClient subscriberDeviceClient;

    @Autowired
    SubscriberTemplateClient subscriberTemplateClient;

    //<editor-fold desc="GET MAPPING">
    @GetMapping("provisioning")
    @PreAuthorize("hasAuthority('API:TAG:GET-PROVISIONING-TAG-BY-DEVICE-TYPE-VERSION-ID')")
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "20") int limit,
                        @RequestParam(value = "view-by", defaultValue = VIEW_BY_TAG) String viewBy,
                        @RequestParam Map<String, String> request) {
        page = page <= 1 ? 0 : page - 1;
        ProvisioningPaginator provisioningPaginator = new ProvisioningPaginator();
        if (VIEW_BY_TAG.equals(viewBy)) {
            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to provisioning page (tag view)","","");
            // TAG VIEW
            TagPaginator rootTagPaginator = tagClient.getPageRootTag(page, limit);

            if (rootTagPaginator.content.size() == 0 && rootTagPaginator.totalPages >= 1) {
                page = rootTagPaginator.totalPages;
                request.put("page", String.valueOf(page));
                rootTagPaginator = tagClient.getPageRootTag(page - 1, limit);
            }

            provisioningPaginator.content = new ArrayList<Provisioning>();
            provisioningPaginator.size = rootTagPaginator.size;
            provisioningPaginator.sort = rootTagPaginator.sort;
            provisioningPaginator.number = ++rootTagPaginator.number;
            provisioningPaginator.totalPages = rootTagPaginator.totalPages;
            provisioningPaginator.totalElements = rootTagPaginator.totalElements;

            DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findAll();
            Map<Long, DeviceTypeVersion> deviceTypeVersionMap = new HashMap<>();
            Map<String, Set<DeviceTypeVersion>> deviceTypeVersionTree = new HashMap<String, Set<DeviceTypeVersion>>();
            for(DeviceTypeVersion deviceTypeVersion: deviceTypeVersions) {
                deviceTypeVersionMap.put(deviceTypeVersion.id, deviceTypeVersion);

                if(ObjectUtils.empty(deviceTypeVersionTree.get(deviceTypeVersion.manufacturer+"-"+deviceTypeVersion.modelName))) {
                    deviceTypeVersionTree.put(deviceTypeVersion.manufacturer+"-"+deviceTypeVersion.modelName, new HashSet<DeviceTypeVersion>());
                }
                deviceTypeVersionTree.get(deviceTypeVersion.manufacturer+"-"+deviceTypeVersion.modelName).add(deviceTypeVersion);
            }

            for (Tag rootTag : rootTagPaginator.content) {
                Tag[] tags = tagClient.getListProvisioningTagByRootTagId(rootTag.id);
                Set<DeviceTypeVersion> deviceTypeVersionSet = new HashSet<DeviceTypeVersion>();
                for (Tag tag: tags) {
                    if (deviceTypeVersionMap.containsKey(tag.deviceTypeVersionId)) {
                        deviceTypeVersionSet.add(deviceTypeVersionMap.get(tag.deviceTypeVersionId));
                    }
                }
                provisioningPaginator.content.add(new Provisioning(rootTag, deviceTypeVersionSet));
            }

            model.addAttribute("provisioningPaginator", provisioningPaginator);
            model.addAttribute("deviceTypeVersionTree", deviceTypeVersionTree);

            return PROVISIONING_VIEW_BY_TAG_PAGE;
        } else {
            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to provisioning page (device view)","","");
            // DEVICE VIEW
            // Get list device type version
            DeviceTypeVersionPaginator deviceTypeVersionPaginator = deviceTypeVersionClient.getPage(page, limit);

            if (deviceTypeVersionPaginator.content.size() == 0 && deviceTypeVersionPaginator.totalPages >= 1) {
                page = deviceTypeVersionPaginator.totalPages;
                request.put("page", String.valueOf(page));
                deviceTypeVersionPaginator = deviceTypeVersionClient.getPage(page - 1, limit);
            }

            provisioningPaginator.content = new ArrayList<Provisioning>();
            provisioningPaginator.size = deviceTypeVersionPaginator.size;
            provisioningPaginator.sort = deviceTypeVersionPaginator.sort;
            provisioningPaginator.number = ++deviceTypeVersionPaginator.number;
            provisioningPaginator.totalPages = deviceTypeVersionPaginator.totalPages;
            provisioningPaginator.totalElements = deviceTypeVersionPaginator.totalElements;

            for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersionPaginator.content) {
                Tag[] tags = deviceTypeVersionClient.findAssignedTagsForDeviceTypeVersion(deviceTypeVersion.id);
                Set<Tag> tagSet = ObjectUtils.empty(tags) ? new HashSet<Tag>() : new HashSet<Tag>(Arrays.asList(tags));
                provisioningPaginator.content.add(new Provisioning(deviceTypeVersion, tagSet));
            }

            // Get list root tag
            Tag[] rootTags = tagClient.getListRootTag();

            model.addAttribute("provisioningPaginator", provisioningPaginator);
            model.addAttribute("rootTags", rootTags);
            model.addAttribute("request", request);

            return PROVISIONING_PAGE;
        }

    }
    //</editor-fold>

    //<editor-fold desc="POST MAPPING">
    @PostMapping("/provisioning/save/{id}")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    public String save(@PathVariable("id") Long rootTagId,
                       @RequestParam Map<String, String> params) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update provisioning tag "+rootTagId,"","");
        if(rootTagId > 0) {
            setEditProvisioning(params, rootTagId);
        } else {
            setCreateProvisioning(params);
        }
        return "redirect:/provisioning";
    }
    //</editor-fold>

    //<editor-fold desc="GET RESPONSE BODY">
    @GetMapping("provisioning/search-root-tag")
    @PreAuthorize("hasAuthority('API:TAG:GET-LIST-PROVISIONING-TAG-BY-ROOT-TAG-ID')")
    @ResponseBody
    public Set<Tag> getSearchRootTag(@RequestParam(value = "q", defaultValue = "") String q) {

        Tag[] rootTags = tagClient.getListRootTag();
        Set<Tag> tags = new LinkedHashSet<Tag>();
        for (Tag tag : rootTags) {
            if (tag.name.contains(q)) {
                tags.add(tag);
            }
        }
        return tags;
    }

    @GetMapping("provisioning/search-device-type-version")
    @PreAuthorize("hasAuthority('API:TAG:FIND-BY-DEVICE-TYPE-VERSION')")
    @ResponseBody
    public Map<String, Set<DeviceTypeVersion>> getSearchDeviceTypeVersion(@RequestParam(value = "q", defaultValue = "") String q) {

        // Get list device type version tree
        DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findAll();
        Map<String, Set<DeviceTypeVersion>> deviceTypeVersionTree = new HashMap<String, Set<DeviceTypeVersion>>();
        for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
            if(ObjectUtils.empty(deviceTypeVersionTree.get(deviceTypeVersion.manufacturer+"-"+deviceTypeVersion.modelName))) {
                deviceTypeVersionTree.put(deviceTypeVersion.manufacturer+" - "+deviceTypeVersion.modelName, new HashSet<DeviceTypeVersion>());
            }
            deviceTypeVersionTree.get(deviceTypeVersion.manufacturer+" - "+deviceTypeVersion.modelName).add(deviceTypeVersion);
        }
        return deviceTypeVersionTree;
    }

    @GetMapping("provisioning/get-form-create")
    @PreAuthorize("hasAuthority('API:TAG:CREATE')")
    @ResponseBody
    public Map<String, Object> getCreate() {
        // Declare data return
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        // Get form data
        getForm(response);

        return response;
    }

    @GetMapping("provisioning/get-form-update")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Map<String, Object> getUpdate(@RequestParam(value = "id", defaultValue = "0") Long rootTagId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"open update provisioning tag popup "+rootTagId,"","");
        // Declare data return
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        // Get form data
        getForm(response);

        //<editor-fold desc="Get data edit">
        // Get tag info
        Tag rootTag = tagClient.get(rootTagId);

        // Get list device type version selected
        Set<Long> deviceTypeVersionIdsSelected = new TreeSet<>();
        Tag[] tags = tagClient.getListProvisioningTagByRootTagId(rootTagId);
        for(Tag tag: tags) {
            deviceTypeVersionIdsSelected.add(tag.deviceTypeVersionId);
        }

        response.put("deviceTypeVersionIdsSelected", deviceTypeVersionIdsSelected);
        response.put("parametersSelected", rootTag.parameters);
        response.put("rootTag", rootTag);
        //</editor-fold>

        return response;

    }

    @GetMapping("/provisioning/{tagId}/existed")
    @PreAuthorize("hasAuthority('API:TAG:READ-ONE-TAG')")
    @ResponseBody
    public Boolean getExisted(@PathVariable("tagId") Long tagId,
                              @RequestParam(value = "name", defaultValue = "") String name) {
        Boolean check = false;
        Tag[] tags = tagClient.findAll();
        for (Tag tag: tags) {
            if (tag.name.equals(name) && !tag.id.equals(tagId) && !tag.rootTagId.equals(tagId)) {
                check = true;
                break;
            }
        }

        return check;
    }

    @GetMapping("/provisioning/get-list-parameter")
    @PreAuthorize("hasAuthority('API:TAG:READ-ONE-TAG')")
    @ResponseBody
    public Map<Long, Set<Parameter>> getListParameter(@RequestParam Map<String, String> params) {

        // Get list device type version id
        Set<Long> deviceTypeVersionIds = new HashSet<>();
        for (String key : params.keySet()) {
            if(key.startsWith("ids")){
                deviceTypeVersionIds.add(Long.valueOf(params.get(key)));
            }
        }

        // Get list parameters by device type version
        Map<Long, Set<Parameter>> parameters = new LinkedHashMap<>();
        DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findAll();
        for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
            if(deviceTypeVersionIds.contains(deviceTypeVersion.id)) {
                for(String path: deviceTypeVersion.parameters.keySet()) {
                    Parameter parameter = deviceTypeVersion.parameters.get(path);
                    if(!("object").equals(parameter.dataType) && Boolean.valueOf(parameter.access)) {

                        if ("dateTime".equals(parameter.dataType)) {
                            String dateTime = ObjectUtils.empty(parameter.value) ? parameter.defaultValue : parameter.value;
                            dateTime = StringUtils.toSampleDate(dateTime);
                            parameter.value = dateTime;
                            parameter.defaultValue = dateTime;
                        }

                        if(ObjectUtils.empty(parameters.get(deviceTypeVersion.id))) {
                            parameters.put(deviceTypeVersion.id, new HashSet<>());
                        }
                        parameters.get(deviceTypeVersion.id).add(parameter);
                    }
                }
            }
        }

        return parameters;
    }

    //</editor-fold>

    //<editor-fold desc="POST RESPONSE BODY">
    @PostMapping("provisioning/post-save-root-tag")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Boolean postCreateRootTag(@RequestParam(value = "tagName", defaultValue = "") String tagName,
                                     @RequestParam(value = "deviceTypeVersionId", defaultValue = "0") Long deviceTypeVersionId) {

        // Get root tag
        Tag rootTag = null;
        Tag[] rootTags = tagClient.getListRootTag();
        for (Tag tag : rootTags) {
            if (tag.name.toLowerCase().equals(tagName.toLowerCase())) {
                rootTag = tag;
                break;
            }
        }
        if (rootTag != null) {
            // Exits root tag | create new tag assigned
            DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);

            Tag tag = new Tag();
            tag.rootTagId = rootTag.id;
            tag.name = tagName;
            tag.assigned = 1;
            tag.deviceTypeVersionId = deviceTypeVersionId;
            tag.parameters = deviceTypeVersion.parameters;
            tagClient.create(tag);
            return true;

        } else {
            // Check tag Name is exits
            Boolean tagExist = false;
            Tag[] tags = deviceTypeVersionClient.findTagsForDeviceTypeVersion(deviceTypeVersionId);
            for (Tag tag : tags) {
                if (tag.name.toLowerCase().equals(tagName.toLowerCase())) {
                    tagExist = true;
                    break;
                }
            }
            if (!tagExist) {
                // Not exits root tag | create new root tag, tag assigned
                DeviceTypeVersion deviceTypeVersion = deviceTypeVersionClient.get(deviceTypeVersionId);

                Tag rootTagNew = new Tag();
                rootTagNew.assigned = 0;
                rootTagNew.name = tagName;
                rootTagNew.deviceTypeVersionId = null;
                rootTagNew = tagClient.create(rootTagNew);

                Tag tag = new Tag();
                tag.rootTagId = rootTagNew.id;
                tag.name = tagName;
                tag.assigned = 1;
                tag.deviceTypeVersionId = deviceTypeVersionId;
                tag.parameters = deviceTypeVersion.parameters;
                tagClient.create(tag);
                return true;

            } else {
                return false;
            }
        }
    }

    @PostMapping("provisioning/post-save-device-type-version")
    @PreAuthorize("hasAuthority('API:TAG:UPDATE')")
    @ResponseBody
    public Boolean postSaveDeviceTypeVersion(@RequestParam(value = "deviceTypeVersionId", defaultValue = "0") Long deviceTypeVersionId,
                                             @RequestParam(value = "rootTagId", defaultValue = "0") Long rootTagId) {


        Tag tag = tagClient.get(rootTagId);
        tag.id = null;
        tag.deviceTypeVersionId = deviceTypeVersionId;
        tag.rootTagId = rootTagId;
        tag.assigned = 1;
        tagClient.create(tag);

        return true;
    }

    @PostMapping("provisioning/post-delete-root-tag")
    @PreAuthorize("hasAuthority('API:TAG:DELETE')")
    @ResponseBody
    public Boolean postDeleteRootTag(@RequestParam(value = "tagId", defaultValue = "0") Long tagId) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove provisioning tag "+tagId,"","");
        tagClient.delete(tagId);
        return true;
    }

    @PostMapping("provisioning/post-delete-device-type-version")
    @PreAuthorize("hasAuthority('API:TAG:DELETE')")
    @ResponseBody
    public Boolean postDeleteDeviceTypeVersion(@RequestParam(value = "deviceTypeVersionId", defaultValue = "0") Long deviceTypeVersionId,
                                               @RequestParam(value = "rootTagId", defaultValue = "0") Long rootTagId) {

        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove provisioning tag "+rootTagId,"","");
        Tag[] tags = tagClient.getListProvisioningTagByRootTagId(rootTagId);
        for (Tag tag: tags) {
            if(tag.deviceTypeVersionId.equals(deviceTypeVersionId)) {
                tagClient.delete(tag.id);
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove provisioning tag "+tag.id,"","");
                break;
            }
        }
        return true;
    }

    @PostMapping("provisioning/post-delete-provisioning-by-device-type-version")
    @PreAuthorize("hasAuthority('API:TAG:DELETE')")
    @ResponseBody
    public Boolean postDeleteRootTagByDeviceTypeVersion(@RequestParam(value = "id[]", defaultValue = "0") Set<Long> deviceTypeVersionIds) {

        Tag[] tags = tagClient.getListAssigned();
        for (Tag tag : tags) {
            if (deviceTypeVersionIds.contains(tag.deviceTypeVersionId)) {
                tagClient.delete(tag.id);
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove provisioning tag "+tag.id,"","");
            }
        }

        return true;
    }

    @PostMapping("provisioning/post-delete-provisioning-by-tag")
    @PreAuthorize("hasAuthority('API:TAG:DELETE')")
    @ResponseBody
    public Boolean postDeleteProvisioningByTag(@RequestParam(value = "id[]", defaultValue = "0") Set<Long> rootTagIds) {

        Tag[] tags = tagClient.getListAssigned();
        for (Tag tag : tags) {
            if (rootTagIds.contains(tag.rootTagId)) {
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove provisioning tag "+tag.id,"","");
                tagClient.delete(tag.id);
            }
        }

        for (Long rootTagId: rootTagIds) {
            tagClient.delete(rootTagId);
            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"remove provisioning tag "+rootTagId,"","");
        }

        return true;
    }
    //</editor-fold>

    //<editor-fold desc="PRIVATE FUNCTION">
    private void setEditProvisioning(Map<String, String> params, Long rootTagId) {
        // Get list device type ids
        Set<Long> deviceTypeVersionIds = new TreeSet<>();
        for (String key: params.keySet()){
            if(key.startsWith("deviceTypeVersion_")) {
                deviceTypeVersionIds.add(Long.valueOf(params.get(key)));
            }
        }

        // Get map parameters by path
        DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findAll();
        Map<String, Parameter> parameterMap = new HashMap<>();
        for (DeviceTypeVersion deviceTypeVersion: deviceTypeVersions) {
            if(deviceTypeVersionIds.contains(deviceTypeVersion.id)) {
                parameterMap.putAll(deviceTypeVersion.parameters);
            }
        }

        // Get tag data
        Tag tag = new Tag();
        Tag rootTag = tagClient.get(rootTagId);
        tag.name = params.get("name");
        rootTag.name = params.get("name");
        rootTag.parameters = new HashMap<String, Parameter>();
        for (String key: params.keySet()){
            if(key.startsWith("path_")) {
                String path = key.substring(5, key.length());

                Parameter parameter = new Parameter();
                if(parameterMap.containsKey(path)) {
                    parameter = parameterMap.get(path);
                } else {
                    parameter.path = path;
                }
                parameter.value = params.get(key);

                // Convert data if data type is date time
                String dataType = params.getOrDefault("dataType_" + path, "");
                if("dateTime".equals(dataType)) {
                    parameter.value = StringUtils.toZoneDateTime(parameter.value);
                }

                if(params.get("useSubscriberData_" + path) != null) {
                    parameter.useSubscriberData = 1;
                    parameter.subscriberData = params.get("subscriberData_" + path);
                }
                rootTag.parameters.put(path, parameter);
            }
        }

        // Save root tag
        tagClient.update(rootTagId, rootTag);
        tag.rootTagId = rootTag.id;
        tag.parameters = rootTag.parameters;
        tag.assigned = 1;

        // Delete tag
        Tag[] tagsOld = tagClient.getListProvisioningTagByRootTagId(rootTagId);
        for (Tag tagOld: tagsOld) {
            if(!deviceTypeVersionIds.contains(tagOld.deviceTypeVersionId)) {
                tagClient.delete(tagOld.id);
            } else {
                tag.deviceTypeVersionId = tagOld.deviceTypeVersionId;
                tag.id = tagOld.id;
                tagClient.update(tag.id, tag);
                deviceTypeVersionIds.remove(tag.deviceTypeVersionId);
            }
        }

        // Save tag
        for (Long deviceVersionId: deviceTypeVersionIds) {
            tag.id = null;
            tag.deviceTypeVersionId = deviceVersionId;
            tagClient.create(tag);
        }
    }

    private void setCreateProvisioning(Map<String, String> params) {
        // Get list device type ids
        Set<Long> deviceTypeVersionIds = new TreeSet<>();
        for (String key: params.keySet()){
            if(key.startsWith("deviceTypeVersion_")) {
                deviceTypeVersionIds.add(Long.valueOf(params.get(key)));
            }
        }

        // Get map parameters by path
        DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findAll();
        Map<String, Parameter> parameterMap = new HashMap<>();
        for (DeviceTypeVersion deviceTypeVersion: deviceTypeVersions) {
            if(deviceTypeVersionIds.contains(deviceTypeVersion.id)) {
                parameterMap.putAll(deviceTypeVersion.parameters);
            }
        }

        // Get tag data
        Tag tag = new Tag();
        Tag rootTag = new Tag();
        tag.name = params.get("name");
        rootTag.name = params.get("name");
        for (String key: params.keySet()){
            if(key.startsWith("path_")) {
                String path = key.substring(5, key.length());
                Parameter parameter = new Parameter();
                if(parameterMap.containsKey(path)) {
                    parameter = parameterMap.get(path);
                } else {
                    parameter.path = path;
                }
                parameter.value = params.get(key);
                if(params.get("useSubscriberData_" + path) != null) {
                    parameter.useSubscriberData = 1;
                    parameter.subscriberData = params.get("subscriberData_" + path);
                }
                rootTag.parameters.put(path, parameter);
            }
        }

        // Save root tag
        rootTag = tagClient.create(rootTag);
        tag.rootTagId = rootTag.id;
        tag.parameters = rootTag.parameters;
        tag.assigned = 1;

        // Save tag
        for (Long deviceVersionId: deviceTypeVersionIds) {
            tag.deviceTypeVersionId = deviceVersionId;
            tagClient.create(tag);
        }
    }

    private void getForm(Map<String, Object> response) {


        // Get list parameter of all device type version
        Map<Long, Set<Parameter>> parameters = new LinkedHashMap<>();

        // Get list device type version tree
        DeviceTypeVersion[] deviceTypeVersions = deviceTypeVersionClient.findAll();
        Map<String, Set<DeviceTypeVersion>> deviceTypeVersionTree = new HashMap<String, Set<DeviceTypeVersion>>();
        for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions) {
            deviceTypeVersion.parameters = new LinkedHashMap<>(); // Xoa du lieu parameter de toi uu
            if(ObjectUtils.empty(deviceTypeVersionTree.get(deviceTypeVersion.manufacturer+"-"+deviceTypeVersion.modelName))) {
                deviceTypeVersionTree.put(deviceTypeVersion.manufacturer+"-"+deviceTypeVersion.modelName, new HashSet<DeviceTypeVersion>());
            }
            deviceTypeVersionTree.get(deviceTypeVersion.manufacturer+"-"+deviceTypeVersion.modelName).add(deviceTypeVersion);

//            for(String path: deviceTypeVersion.parameters.keySet()) {
//                Parameter parameter = deviceTypeVersion.parameters.get(path);
//                if(!("object").equals(parameter.dataType) && Boolean.valueOf(parameter.access)) {
//                    if(ObjectUtils.empty(parameters.get(deviceTypeVersion.id))) {
//                        parameters.put(deviceTypeVersion.id, new HashSet<>());
//                    }
//                    parameters.get(deviceTypeVersion.id).add(parameter);
//                }
//            }
        }

        // Get list profile
        Tag[] profiles = tagClient.getListProfileSynchronized();

        // Get list subscriber templates
        SubscriberTemplate[] subscriberTemplates = subscriberTemplateClient.findAll();

        response.put("deviceTypeVersionTree", deviceTypeVersionTree);
        response.put("profiles", profiles);
        response.put("subscriberTemplates", subscriberTemplates);
        response.put("parameters", parameters);
    }
    //</editor-fold>
}
