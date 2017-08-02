package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.client.ParameterDetailClient;
import vn.vnpt.ssdc.api.client.PolicyJobClient;
import vn.vnpt.ssdc.api.client.PolicyTaskClient;
import vn.vnpt.ssdc.api.client.UserClient;
import vn.vnpt.ssdc.api.model.AcsResponse;
import vn.vnpt.ssdc.api.model.DeviceGroup;
import vn.vnpt.ssdc.api.model.Parameter;
import vn.vnpt.ssdc.api.model.ParameterDetail;
import vn.vnpt.ssdc.api.model.PolicyJob;
import vn.vnpt.ssdc.api.model.PolicyTask;
import vn.vnpt.ssdc.api.model.User;
import vn.vnpt.ssdc.models.FileManagement;
import vn.vnpt.ssdc.models.PolicyJobPaginator;
import vn.vnpt.ssdc.policy.parser.FileTypeNotSupportedException;
import vn.vnpt.ssdc.policy.parser.PolicyParser;
import vn.vnpt.ssdc.policy.parser.PolicyParserFactory;
import vn.vnpt.ssdc.umpexception.PolicyFileFormatException;
import vn.vnpt.ssdc.utils.ObjectUtils;
import vn.vnpt.ssdc.utils.StringUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class PolicyController extends BaseController {

    @Autowired
    DeviceGroupClient deviceGroupClient;

    @Autowired
    PolicyParserFactory policyParserFactory;

    @Autowired
    PolicyJobClient policyJobClient;

    @Autowired
    PolicyTaskClient policyTaskClient;

    @Autowired
    ParameterDetailClient parameterDetailClient;

    @Autowired
    UserClient userClient;

    private Logger logger = LoggerFactory.getLogger(DataModelController.class);

    private static final String POLICY_URL = "/policy";
    private static final String POLICY_PAGE = "policy/policy";

    private static final String POLICY_LOG_URL = "/policy/log";
    private static final String POLICY_LOG_PAGE = "policy/policy_log";

    private static final String POLICY_CONFIGURATION_URL = "/policy-configuration";
    private static final String CREATE_POLICY_CONFIGURATION_PAGE = "policy/create_policy_configuration";
    private static final String EDIT_POLICY_CONFIGURATION_PAGE = "policy/edit_policy_configuration";
    private static final String SHOW_POLICY_CONFIGURATION_PAGE = "policy/show_policy_configuration";

    private static final String POLICY_OPERATION_URL = "/policy-operation";
    private static final String CREATE_POLICY_OPERATION_PAGE = "policy/create_policy_operation";
    private static final String SHOW_POLICY_OPERATION_PAGE = "policy/show_policy_operation";
    private static final String EDIT_POLICY_OPERATION_PAGE = "policy/edit_policy_operation";


    //<editor-fold desc="GET MAPPING">

    @GetMapping(POLICY_URL)
    @PreAuthorize("hasAuthority('API:POLICY:READ-LIST-POLICIES')")
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "20") int limit) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to policy page","","");
        page--;
        PolicyJobPaginator policyJobPaginator = policyJobClient.getPageWithNumberOfExecution(page, limit);
        policyJobPaginator.number++;
        model.addAttribute("policyJobPaginator", policyJobPaginator);

        return POLICY_PAGE;
    }

    @GetMapping(POLICY_CONFIGURATION_URL)
    @PreAuthorize("hasAuthority('API:POLICY:CREATE')")
    public String createPolicyConfiguration(Model model) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to create configuration policy page","","");
        model.addAttribute("deviceGroups", deviceGroupClient.findAll());

        return CREATE_POLICY_CONFIGURATION_PAGE;
    }

    @GetMapping(POLICY_CONFIGURATION_URL + "/{id}/edit")
    @PreAuthorize("hasAuthority('API:POLICY:UPDATE')")
    public String editPolicyConfiguration(@PathVariable("id") Long id,
                                          Model model) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to update configuration policy page","","");
        PolicyJob policyJob = policyJobClient.get(id);
        List<String> policyParametersList = new ArrayList<>(policyJob.parameters.keySet());
        String policyParameters = String.join(",", policyParametersList);

        model.addAttribute("policyJob", policyJob);
        model.addAttribute("deviceGroups", deviceGroupClient.findAll());
        model.addAttribute("policyParameters", policyParameters);

        return EDIT_POLICY_CONFIGURATION_PAGE;
    }

    @GetMapping(POLICY_OPERATION_URL)
    @PreAuthorize("hasAuthority('API:POLICY:CREATE')")
    public String createPolicyOperation(Model model) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to create operation policy page","","");
        getFormCreatePolicyOperation(model);
        return CREATE_POLICY_OPERATION_PAGE;
    }

    @GetMapping(POLICY_OPERATION_URL + "/{id}" + "/edit")
    @PreAuthorize("hasAuthority('API:POLICY:UPDATE')")
    public String editPolicyOperation(Model model,
                                      @PathVariable("id") Long id) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to update operation policy page","","");
        getFormCreatePolicyOperation(model);
        PolicyJob policyJob = policyJobClient.get(id);
        model.addAttribute("policyJob", policyJob);

        return EDIT_POLICY_OPERATION_PAGE;
    }

    @GetMapping(POLICY_CONFIGURATION_URL + "/{id}")
    @PreAuthorize("hasAuthority('API:POLICY:READ-ONE-POLICY')")
    public String showPolicyConfiguration(Model model,
                                          @PathVariable("id") Long id) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"show a configuration policy","","");
        getPolicy(model, id);
        return SHOW_POLICY_CONFIGURATION_PAGE;
    }

    @GetMapping(POLICY_LOG_URL + "/{id}")
    @PreAuthorize("hasAuthority('API:POLICY:READ-ONE-POLICY')")
    public String showPolicyLog(Model model,
                                @PathVariable("id") Long id,
                                @RequestParam(value = "page", defaultValue = "1") int page,
                                @RequestParam(value = "limit", defaultValue = "20") int limit,
                                @RequestParam Map<String, String> request) {

        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"show log of policy","","");
        PolicyTask[] policyTasks= policyTaskClient.getPage(id, page, limit);
        for (PolicyTask policyTask : policyTasks) {
            try {
                String[] subDeviceId = policyTask.deviceId.split("-");
                policyTask.setOui(subDeviceId[0]);
                policyTask.setProductClass(subDeviceId[1]);
                policyTask.setSerialNumber(subDeviceId[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PolicyJob policyJob = policyJobClient.get(id);
        model.addAttribute("policyTasks", policyTasks);
        model.addAttribute("policyJob", policyJob);
        model.addAttribute("summary", policyTaskClient.getSummary(id));
        model.addAttribute("request", request);

        return POLICY_LOG_PAGE;
    }

    @GetMapping(POLICY_OPERATION_URL + "/{id}")
    @PreAuthorize("hasAuthority('API:POLICY:READ-ONE-POLICY')")
    public String showPolicyOperation(Model model,
                                      @PathVariable("id") Long id) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"show a operation policy","","");
        getPolicy(model, id);
        return SHOW_POLICY_OPERATION_PAGE;
    }

    //</editor-fold>

    //<editor-fold desc="POST MAPPING">

    @PostMapping(POLICY_CONFIGURATION_URL + "/save")
    @PreAuthorize("hasAuthority('API:POLICY:UPDATE')")
    public String storePolicyConfiguration(@RequestParam Map<String, String> params,
                                           @RequestParam(value = "externalFile", required = false) MultipartFile externalFile) {

        try {
            PolicyJob policyJob = new PolicyJob();
            setPolicyConfiguration(policyJob, params, externalFile);
            policyJob = policyJobClient.create(policyJob);
            return "redirect:" + POLICY_CONFIGURATION_URL + "/" + policyJob.id;

        } catch (PolicyFileFormatException e) {
            return "redirect:" + POLICY_CONFIGURATION_URL + "?PolicyFileFormatException=" + e.getMessage();
        }
    }

    @PostMapping(POLICY_CONFIGURATION_URL + "/{id}/update")
    @PreAuthorize("hasAuthority('API:POLICY:UPDATE')")
    public String updatePolicyConfiguration(@PathVariable("id") Long id,
                                            @RequestParam Map<String, String> params,
                                            @RequestParam(value = "externalFile", required = false) MultipartFile externalFile) {
        try {
            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update configuration policy page","","");
            PolicyJob policyJob = policyJobClient.get(id);
            setPolicyConfiguration(policyJob, params, externalFile);
            policyJobClient.update(id, policyJob);
            return "redirect:" + POLICY_CONFIGURATION_URL + "/" + policyJob.id;

        } catch (PolicyFileFormatException e) {
            return "redirect:" + POLICY_CONFIGURATION_URL + "?PolicyFileFormatException=" + e.getMessage();
        }
    }

    @PostMapping(POLICY_OPERATION_URL + "/save")
    public String storePolicyOperation(@RequestParam Map<String, String> params,
                                       @RequestParam(value = "externalFile", required = false) MultipartFile externalFile) {
        try {
            PolicyJob policyJob = new PolicyJob();
            setPolicyOperation(policyJob, params, externalFile);
            policyJob = policyJobClient.create(policyJob);
            return "redirect:" + POLICY_OPERATION_URL + "/" + policyJob.id;

        } catch (PolicyFileFormatException e) {
            return "redirect:" + POLICY_CONFIGURATION_URL + "?PolicyFileFormatException=" + e.getMessage();
        }
    }

    @PostMapping(POLICY_OPERATION_URL + "/{id}/update")
    @PreAuthorize("hasAuthority('API:POLICY:UPDATE')")
    public String updatePolicyOperation(@PathVariable("id") Long id,
                                        @RequestParam Map<String, String> params,
                                        @RequestParam(value = "externalFile", required = false) MultipartFile externalFile) {
        try {
            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update operation policy page","","");
            PolicyJob policyJob = policyJobClient.get(id);
            setPolicyOperation(policyJob, params, externalFile);
            policyJobClient.update(id, policyJob);
            return "redirect:" + POLICY_OPERATION_URL + "/" + policyJob.id;

        } catch (PolicyFileFormatException e) {
            return "redirect:" + POLICY_CONFIGURATION_URL + "?PolicyFileFormatException=" + e.getMessage();
        }
    }

    //</editor-fold>

    //<editor-fold desc="GET RESPONSE BODY">

    @GetMapping(POLICY_CONFIGURATION_URL + "/search-parameters")
    @ResponseBody
    public ParameterDetail[] getSearchParameters(@RequestParam(value = "path", defaultValue = "") String path) {
        return parameterDetailClient.findParameters();
    }

    @GetMapping(POLICY_URL + "/{id}/get-existed")
    @ResponseBody
    public Boolean getExisted(@PathVariable("id") Long id,
                              @RequestParam(value = "name", defaultValue = "") String name) {

        Boolean exist = false;

        PolicyJob[] policyJobs = policyJobClient.findAll();
        for (PolicyJob policyJob : policyJobs) {
            if (name.equals(policyJob.name) && !id.equals(policyJob.id)) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    @GetMapping(POLICY_URL + "/{id}/get-existed-priority")
    @ResponseBody
    public Boolean getExistedPriority(@PathVariable("id") Long id,
                                      @RequestParam(value = "priority", defaultValue = "") String priority) {

        Boolean exist = false;

        PolicyJob[] policyJobs = policyJobClient.findAll();
        for (PolicyJob policyJob : policyJobs) {
            if (priority.equals(String.valueOf(policyJob.priority)) && !id.equals(policyJob.id)) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    @GetMapping(POLICY_URL + "/{id}")
    @ResponseBody
    public PolicyJob getDetail(@PathVariable("id") Long id) {
        return policyJobClient.get(id);
    }

    //</editor-fold>

    //<editor-fold desc="POST RESPONSE BODY">
    @PostMapping(POLICY_URL + "/delete")
    @PreAuthorize("hasAuthority('API:POLICY:DELETE')")
    @ResponseBody
    public Boolean postDelete(@RequestParam Map<String, String> params) {
        for(String key : params.keySet()) {
            if(key.startsWith("id")) {
                policyJobClient.delete(Long.valueOf(params.get(key)));
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete policy "+params.get(key),"","");
            }
        }

        return true;
    }

    @PostMapping(POLICY_URL + "/{id}/execute")
    @PreAuthorize("hasAuthority('API:POLICY:EXCUTE')")
    @ResponseBody
    public Boolean postExecute(@PathVariable("id") Long id) {
        policyJobClient.execute(id);
        PolicyJob policyJob = policyJobClient.get(id);
        policyJob.status = "EXECUTE";
        policyJobClient.update(id, policyJob);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"excute policy "+id,"","");
        return true;
    }

    @PostMapping(POLICY_URL + "/{id}/stop")
    @PreAuthorize("hasAuthority('API:POLICY:STOP')")
    @ResponseBody
    public Boolean postStop(@PathVariable("id") Long id) {
        policyJobClient.stop(id);
        PolicyJob policyJob = policyJobClient.get(id);
        policyJob.status = "STOP";
        policyJobClient.update(id, policyJob);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"stop policy "+id,"","");
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="PRIVATE FUNCTION">

    private void getPolicy(Model model, Long id) {
        PolicyJob policyJob = policyJobClient.get(id);
        DeviceGroup deviceGroup = null;
        if(policyJob != null && policyJob.deviceGroupId != null) {
            deviceGroup = deviceGroupClient.get(policyJob.deviceGroupId);
        }

        model.addAttribute("policyJob", policyJob);
        model.addAttribute("deviceGroup", deviceGroup);
    }

    private void getExternalDevices(PolicyJob policyJob, MultipartFile multipartFile){
        if (multipartFile != null && !multipartFile.getOriginalFilename().isEmpty()) {
            PolicyParser parser = null;

            try {
                parser = (PolicyParser) policyParserFactory.getParser(multipartFile.getOriginalFilename());
                policyJob.externalDevices = parser.parse(multipartFile.getInputStream());
                policyJob.externalFilename = multipartFile.getOriginalFilename();

            } catch (FileTypeNotSupportedException e) {
                e.printStackTrace();
            } catch (IOException e) {
            }
        }
    }

    private Timestamp stringToTimestamp(String dataFormat, String dateString) {
        final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        Timestamp ts = null;
        try {
            String newDateString;
            DateFormat formatter = new SimpleDateFormat(dataFormat);
            Date d = null;
            d = formatter.parse(dateString);
            ((SimpleDateFormat) formatter).applyPattern(NEW_FORMAT);
            newDateString = formatter.format(d);
            ts = Timestamp.valueOf(newDateString);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ts;
    }

    private void setParameters(PolicyJob policyJob, Map<String, String> params) {

        ParameterDetail[] parameterDetails = parameterDetailClient.findAll();
        Map<String, Parameter> parameterMap = new HashMap<>();
        for (ParameterDetail parameterDetail : parameterDetails) {
            parameterMap.put(parameterDetail.path, parameterDetail.toParameter());
        }

        policyJob.parameters = new HashMap<String, Object>();
        for(String key : params.keySet()) {
            if (key.startsWith("path_")) {
                String path = key.substring("path_".length(), key.length());
                String value = params.get(key);

                Parameter parameter = new Parameter();
                if(parameterMap.containsKey(path)) {
                    parameter = parameterMap.get(path);
                } else {
                    parameter.path = path;
                }
                parameter.value = value;
                policyJob.parameters.put(path, parameter);
            }
        }
    }

    private void getAction(PolicyJob policyJob, Map<String, String> params) {
        String actionName = params.get("actionName");
        policyJob.actionName = actionName;

        switch (actionName) {
            case "reboot":
            case "factoryReset":
                policyJob.parameters = new HashMap<String, Object>();
                break;

            case "restore":
            case "backup":
                policyJob.parameters = new HashMap<String, Object>();
                policyJob.parameters.put("fileType", params.get("fileType"));
                break;

            case "updateFirmware":
            case "downloadVendorConfigurationFile":
                policyJob.parameters.put("fileId", params.get("fileId"));
                break;

            default:
                policyJob.parameters = new HashMap<String, Object>();
                break;
        }
    }

    private void setPolicyOperation(PolicyJob policyJob, Map<String, String> params, MultipartFile externalFile) {

        policyJob.name = params.get("name");
        policyJob.priority = Integer.valueOf(params.get("priority"));
        policyJob.status = "INIT";

        // Check selectedTargetedDevices choose
        String selectedTargetedDevices = params.get("selectedTargetedDevices");
        if (selectedTargetedDevices != null) {
            if ("byFilters".equals(selectedTargetedDevices)) {
                policyJob.deviceGroupId = Long.valueOf(params.get("deviceGroupId"));
            } else if ("byExternalFile".equals(selectedTargetedDevices)) {
                getExternalDevices(policyJob, externalFile);
            }
        }

        // Check schedule choose
        String schedule = params.get("schedule");
        if (schedule != null) {
            if ("startAt".equals(schedule)) {

                policyJob.isImmediately = false;
                policyJob.timeInterval = Integer.valueOf(params.get("timeInterval"));
                policyJob.maxNumber = Integer.valueOf(params.get("maxNumber"));
                policyJob.events = new ArrayList<String>();
                for (String key : params.keySet()) {
                    if (key.startsWith("events_")) {
                        policyJob.events.add(params.get(key));
                    }
                }

                Timestamp ts = stringToTimestamp("dd-MM-yyyy HH:mm", params.get("startAt"));
                if (!ObjectUtils.empty(ts)) {
                    policyJob.startAt = ts.getTime();
                }
                if(!params.get("limited").isEmpty()) {
                    policyJob.limited = Integer.valueOf(params.get("limited"));
                }

            } else if ("immediately".equals(schedule)) {
                policyJob.isImmediately = true;
            }
        }

        // Get parameters
        getAction(policyJob, params);
    }

    private void setPolicyConfiguration(PolicyJob policyJob, Map<String, String> params, MultipartFile externalFile) {
        policyJob.actionName = "parameters";
        policyJob.name = params.get("name");
        policyJob.priority = Integer.valueOf(params.get("priority"));
        policyJob.status = "INIT";

        // Check selectedTargetedDevices choose
        String selectedTargetedDevices = params.get("selectedTargetedDevices");
        if(selectedTargetedDevices != null) {
            if("byFilters".equals(selectedTargetedDevices)) {
                policyJob.deviceGroupId = Long.valueOf(params.get("deviceGroupId"));

            } else if("byExternalFile".equals(selectedTargetedDevices)) {
                getExternalDevices(policyJob, externalFile);

            }
        }

        // Check schedule choose
        String schedule = params.get("schedule");
        if(schedule != null) {
            if("startAt".equals(schedule)) {

                policyJob.isImmediately = false;
                policyJob.timeInterval = Integer.valueOf(params.get("timeInterval"));
                policyJob.maxNumber = Integer.valueOf(params.get("maxNumber"));
                policyJob.events = new ArrayList<String>();
                for(String key : params.keySet()) {
                    if(key.startsWith("events_")) {
                        policyJob.events.add(params.get(key));
                    }
                }

                Timestamp ts = stringToTimestamp("dd-MM-yyyy HH:mm", params.get("startAt"));
                if(!ObjectUtils.empty(ts)) {
                    policyJob.startAt = ts.getTime();
                }
                if(!params.get("limited").isEmpty()) {
                    policyJob.limited = Integer.valueOf(params.get("limited"));
                }

            } else if("immediately".equals(schedule)) {
                policyJob.isImmediately = true;
            }
        }

        // Get parameters
        setParameters(policyJob, params);
    }

    private void getFormCreatePolicyOperation(Model model) {
        // Get list device group
        model.addAttribute("deviceGroups", deviceGroupClient.findAll());

        // Get list firmware files
        Map<String, String> indexParams = fileManagementIndexParamsForSelect();
        Map<String, String> acsQuery = new HashMap<>();
        acsQuery.put("query", "{\"metadata.fileType\" : \"1 Firmware Upgrade Image\"}");
        acsQuery.put("parameters", String.join(",", indexParams.keySet()));
        AcsResponse response = acsApiClient.getAllFiles(acsQuery);
        List<FileManagement> firmwareFles = FileManagement.fromJsonString(response.body, indexParams.keySet());

        Map<String, Set<FileManagement>> firmwareFileManagementMap = new HashMap<String, Set<FileManagement>>();
        for (FileManagement fileManagement : firmwareFles) {
            String fileKey = fileManagement.oui() + " - " + fileManagement.productClass();
            Set<FileManagement> fileSet = firmwareFileManagementMap.containsKey(fileKey) ? firmwareFileManagementMap.get(fileKey) : new HashSet<FileManagement>();

            fileSet.add(fileManagement);
            firmwareFileManagementMap.put(fileKey, fileSet);
        }

        // Get list firmware files
        acsQuery = new HashMap<>();
        acsQuery.put("query", "{\"metadata.fileType\" : \"3 Vendor Configuration File\"}");
        acsQuery.put("parameters", String.join(",", indexParams.keySet()));
        response = acsApiClient.getAllFiles(acsQuery);
        List<FileManagement> vendorFiles = FileManagement.fromJsonString(response.body, indexParams.keySet());

        Map<String, Set<FileManagement>> vendorFileManagementMap = new HashMap<String, Set<FileManagement>>();
        for (FileManagement fileManagement : vendorFiles) {
            String fileKey = fileManagement.oui() + " - " + fileManagement.productClass();
            Set<FileManagement> fileSet = vendorFileManagementMap.containsKey(fileKey) ? vendorFileManagementMap.get(fileKey) : new HashSet<FileManagement>();

            fileSet.add(fileManagement);
            vendorFileManagementMap.put(fileKey, fileSet);
        }

        model.addAttribute("vendorFileManagementMap", vendorFileManagementMap);
        model.addAttribute("firmwareFileManagementMap", firmwareFileManagementMap);
    }

    //</editor-fold>


}
