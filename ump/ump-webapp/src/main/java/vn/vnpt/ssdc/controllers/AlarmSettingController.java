package vn.vnpt.ssdc.controllers;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.AlarmClient;
import vn.vnpt.ssdc.api.client.AlarmDetailClient;
import vn.vnpt.ssdc.api.client.AlarmTypeClient;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.model.*;
import vn.vnpt.ssdc.models.AlarmSettingPaginator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by thangnc on 23-May-17.
 */
@Controller
public class AlarmSettingController extends BaseController{
    private Logger logger = LoggerFactory.getLogger(AlarmSettingController.class);
    @Autowired
    DeviceGroupClient deviceGroupClient;

    @Autowired
    AlarmTypeClient alarmTypeClient;

    @Autowired
    AlarmClient alarmClient;

    @Autowired
    AlarmDetailClient alarmDetailClient;

    @GetMapping("/alarm-setting")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:READ-LIST-ALARM-TYPES')")
    public String alarm_setting(Model model, @RequestParam Map<String, String> requestParams) throws UnsupportedEncodingException {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to alarm setting page","","");
        AlarmSettingPaginator alarmSettingPaginator = new AlarmSettingPaginator();
        alarmSettingPaginator.alarmTypeClient = alarmTypeClient;
        alarmSettingPaginator.indexParams = alarmSettingIndexParams();
        alarmSettingPaginator.parseParam((HashMap<String, String>) requestParams);
        alarmSettingPaginator.loadResult(requestParams);

        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        model.addAttribute("search", false);
        model.addAttribute("deviceGroups", deviceGroups);
        model.addAttribute("paginator", alarmSettingPaginator);
        return "alarm/alarm_setting";
    }

    @GetMapping("/alarm-setting/search")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:READ-LIST-ALARM-TYPES')")
    public String logging(Model model, @RequestParam Map<String, String> requestParams) throws UnsupportedEncodingException {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"search on alarm setting page","","");
        AlarmSettingPaginator alarmSettingPaginator = new AlarmSettingPaginator();
        alarmSettingPaginator.alarmTypeClient = alarmTypeClient;
        alarmSettingPaginator.indexParams = alarmSettingIndexParams();
        alarmSettingPaginator.loadResultSearch(requestParams);

        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        model.addAttribute("search", true);
        model.addAttribute("paginator", alarmSettingPaginator);
        model.addAttribute("oldSeverity", requestParams.get("severity"));
        model.addAttribute("oldAlarmType", requestParams.get("alarmType"));
        model.addAttribute("oldAlarmName", requestParams.get("alarmName"));
        if(requestParams.get("group") != null && !("null").equals(requestParams.get("group")) && !("").equals(requestParams.get("group"))) {
            DeviceGroup deviceGroup = deviceGroupClient.get(Long.parseLong(requestParams.get("group")));
            model.addAttribute("oldGroup", deviceGroup.name);
            model.addAttribute("oldGroupId", deviceGroup.id);
            for(int i = 0; i < deviceGroups.length; i++) {
                if(deviceGroups[i].id == deviceGroup.id) {
                    List<DeviceGroup> list =  new ArrayList<>();
                    Collections.addAll(list, deviceGroups);
                    list.remove(deviceGroups[i]);
                    deviceGroups = list.toArray(new DeviceGroup[list.size()]);
                }
            }
        } else {
            model.addAttribute("oldGroup", "");
            model.addAttribute("oldGroupId", "");
        }
        model.addAttribute("deviceGroups", deviceGroups);
        model.addAttribute("oldLabel", requestParams.get("textSearch"));
        return "alarm/alarm_setting";
    }

    @PostMapping("/alarm-setting/create")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:CREATE')")
    public String createAlarm(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create a new alarm setting","","");
        Set<DeviceGroup> deviceGroups = new LinkedHashSet<>();
        AlarmType alarmType = new AlarmType();
        alarmType.type = requestParams.get("alarmType");
        alarmType.name = requestParams.get("alarmName");
        String[] listGroupFilter = requestParams.get("listGroupFilter").split(",");
        if(listGroupFilter.length > 0) {
            for(int i = 0; i < listGroupFilter.length; i++) {
                DeviceGroup deviceGroup = deviceGroupClient.get(Long.parseLong(listGroupFilter[i]));
                deviceGroups.add(deviceGroup);
            }
            alarmType.deviceGroups = deviceGroups;
        }
        alarmType.severity = requestParams.get("alarmSeverity");
        if(!("").equals(requestParams.get("aggregatedVolume"))) alarmType.aggregatedVolume = Long.parseLong(requestParams.get("aggregatedVolume"));
        if(!("").equals(requestParams.get("notifyAggregated"))) alarmType.notifyAggregated = requestParams.get("notifyAggregated");
        if(("on").equals(requestParams.get("alarmNotifyCreate"))) alarmType.notify = true;
        else alarmType.notify = false;
        if(("on").equals(requestParams.get("alarmMonitorCreate"))) alarmType.monitor = true;
        else alarmType.monitor = false;
        if(!("").equals(requestParams.get("tableParameter"))) {
            String[] tableParameter = requestParams.get("tableParameter").split(",");
            Map<String, String> parameter_values = new HashMap<>();
            for (int i = 0; i < tableParameter.length; i++)
            {
                String parameter = tableParameter[i];
                parameter_values.put(parameter.split("\\|")[0], parameter.split("\\|")[1]);
            }
            alarmType.parameterValues = parameter_values;
        }

        if(alarmType.type.equals("PARAMETER_VALUE")) {
            alarmType.notification = Integer.parseInt(requestParams.get("notification"));
            if(alarmType.notification == 1) {
                if(requestParams.get("time") != null && !("").equals(requestParams.get("time"))) {
                    alarmType.timeSettings = Integer.parseInt(requestParams.get("time"));
                } else alarmType.timeSettings = 5;
            }
        }
        alarmTypeClient.create(alarmType);
        return "redirect:/alarm-setting";
    }

    @PostMapping("/alarm-setting/edit")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:UPDATE')")
    public String editAlarm(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update alarm setting","","");
        Set<DeviceGroup> deviceGroups = new LinkedHashSet<>();
        AlarmType alarmType = new AlarmType();
        alarmType.id = Long.parseLong(requestParams.get("alarmId"));
        alarmType.type = requestParams.get("alarmType");
        alarmType.name = requestParams.get("alarmName");
        String[] listGroupFilter = requestParams.get("listGroupFilter").split(",");
        if(listGroupFilter.length > 0) {
            for(int i = 0; i < listGroupFilter.length; i++) {
                DeviceGroup deviceGroup = deviceGroupClient.get(Long.parseLong(listGroupFilter[i]));
                deviceGroups.add(deviceGroup);
            }
            alarmType.deviceGroups = deviceGroups;
        }
        alarmType.severity = requestParams.get("alarmSeverity");
        if(!("").equals(requestParams.get("aggregatedVolume"))) alarmType.aggregatedVolume = Long.parseLong(requestParams.get("aggregatedVolume"));
        if(!("").equals(requestParams.get("notifyAggregated"))) alarmType.notifyAggregated = requestParams.get("notifyAggregated");
        if(("on").equals(requestParams.get("alarmNotify"))) alarmType.notify = true;
        else alarmType.notify = false;
        if(("on").equals(requestParams.get("alarmMonitor"))) alarmType.monitor = true;
        else alarmType.monitor = false;
        if(!("").equals(requestParams.get("tableParameter"))) {
            String[] tableParameter = requestParams.get("tableParameter").split(",");
            Map<String, String> parameter_values = new HashMap<>();
            for (int i = 0; i < tableParameter.length; i++)
            {
                String parameter = tableParameter[i];
                parameter_values.put(parameter.split("\\|")[0], parameter.split("\\|")[1]);
            }
            alarmType.parameterValues = parameter_values;
        }
        if(alarmType.type.equals("PARAMETER_VALUE")) {
            alarmType.notification = Integer.parseInt(requestParams.get("editNotification"));
            if(alarmType.notification == 1) {
                if(requestParams.get("editTime") != null && !("").equals(requestParams.get("editTime"))) {
                    alarmType.timeSettings = Integer.parseInt(requestParams.get("editTime"));
                } else alarmType.timeSettings = 5;
            }
        }
        alarmTypeClient.update(alarmType.id,alarmType);
        return "redirect:/alarm-setting";
    }

    @PostMapping("/alarm-setting/{alarmName}/check")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:READ-ONE-ALARM-TYPE')")
    @ResponseBody
    public  AcsResponse checkAlarmName(Model model, @PathVariable("alarmName") String alarmName) throws UnsupportedEncodingException {
        AcsResponse result = new AcsResponse();
        try {
            AlarmType[] alarmTypes = alarmTypeClient.getAlarmByName(alarmName);
            if(alarmTypes.length > 0) result.mapResult.put(alarmName, String.valueOf(200));
        } catch (Exception ex) {
            result.mapResult.put(alarmName, String.valueOf(404));
        }
        return result;
    }

    @PostMapping("/alarm-setting/remove")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:DELETE')")
    @ResponseBody
    public AcsResponse removeAlarm(Model model, @RequestBody String listId) throws UnsupportedEncodingException {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete a setting alarm","","");
        String lst = URLDecoder.decode(listId, "UTF-8");
        AcsResponse result = new AcsResponse();
        String regex = "\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lst);
        while (matcher.find()) {
            String alarmID = matcher.group();
            alarmID = alarmID.replaceAll("\"", "");
            try {
                alarmTypeClient.delete(Long.parseLong(alarmID));
                Alarm[] alarms = alarmClient.getAlarmNameByAlarmId(Long.parseLong(alarmID));
                for(int i = 0; i < alarms.length; i++) alarmClient.delete(alarms[i].id);
                AlarmDetails[] alarmDetails = alarmDetailClient.getAlarmDetailByAlarmId(Long.parseLong(alarmID));
                for(int i = 0; i < alarms.length; i++) alarmDetailClient.delete(alarmDetails[i].id);
                result.mapResult.put(alarmID, String.valueOf(200));
            } catch (Exception ex) {
                result.mapResult.put(alarmID, String.valueOf(404));
            }
        }
        return result;
    }

    @PostMapping("/alarm-setting/getListAlarmByType")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:READ-ONE-ALARM-TYPE')")
    @ResponseBody
    public AcsResponse getListAlarmByType(Model model, @RequestBody String listType) throws UnsupportedEncodingException {
        String lst = URLDecoder.decode(listType, "UTF-8");
        AcsResponse result = new AcsResponse();
        String regex = "\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lst);
        while (matcher.find()) {
            String type = matcher.group();
            type = type.replaceAll("\"", "");
            try {
                AlarmType[] alarmTypes = alarmTypeClient.search(type,"","","","");
                for (int i = 0; i < alarmTypes.length; i++) {
                    result.mapResult.put(alarmTypes[i].id + "", alarmTypes[i].name);
                }
            } catch (Exception ex) {
                result.mapResult.put(type, String.valueOf(404));
            }
        }
        return result;
    }

    @PostMapping("/alarm-setting/checkNameExited")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:READ-ONE-ALARM-TYPE')")
    @ResponseBody
    public  AcsResponse checkNameExited(Model model, @RequestBody String listType) throws UnsupportedEncodingException {
        String lst = URLDecoder.decode(listType, "UTF-8");
        AcsResponse result = new AcsResponse();
        String regex = "\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lst);
        while (matcher.find()) {
            String name = matcher.group();
            name = name.replaceAll("\"", "");
            try {
                AlarmType[] alarmTypes = alarmTypeClient.getAlarmByName(name);
                if(alarmTypes != null && alarmTypes.length > 0)
                    result.mapResult.put(name, String.valueOf(200));
            } catch (Exception ex) {
                result.mapResult.put(name, String.valueOf(404));
            }
        }
        return result;
    }

    @PostMapping("/alarm-setting/{alarmId}/checkNameEditExited")
    @PreAuthorize("hasAuthority('API:ALARM-TYPE:READ-ONE-ALARM-TYPE')")
    @ResponseBody
    public  AcsResponse checkNameEditExited(Model model, @RequestBody String listType, @PathVariable("alarmId") long alarmId) throws UnsupportedEncodingException {
        String lst = URLDecoder.decode(listType, "UTF-8");
        AcsResponse result = new AcsResponse();
        String regex = "\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lst);
        while (matcher.find()) {
            String name = matcher.group();
            name = name.replaceAll("\"", "");
            try {
                AlarmType[] alarmTypes = alarmTypeClient.getAlarmByName(name);
                if(alarmTypes != null && alarmTypes.length > 0 && alarmTypes[0].id != alarmId)
                    result.mapResult.put(name, String.valueOf(200));
            } catch (Exception ex) {
                result.mapResult.put(name, String.valueOf(404));
            }
        }
        return result;
    }

}
