package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.AlarmClient;
import vn.vnpt.ssdc.api.client.AlarmTypeClient;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.model.Alarm;
import vn.vnpt.ssdc.api.model.AlarmType;
import vn.vnpt.ssdc.api.model.DeviceGroup;
import vn.vnpt.ssdc.models.AlarmPaginator;
import vn.vnpt.ssdc.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lamborgini on 5/23/2017.
 */
@Controller
public class AlarmListController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(AlarmListController.class);
    private static final String ALARM_LIST_PAGE = "alarm_dashboard/alarm_dashboard";

    @Autowired
    private AlarmTypeClient alarmTypeClient;

    @Autowired
    private AlarmClient alarmClient;

    @Autowired
    private DeviceGroupClient deviceGroupClient;

    @GetMapping("/alarm")
    @PreAuthorize("hasAuthority('API:ALARM:READ-LIST-ALARMS')")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}", session.getId(), session.getAttribute("username"), "go to list alarm page", "", "");
        loadData(model, requestParams);
        return ALARM_LIST_PAGE;
    }

    @GetMapping("/alarm/search")
    @PreAuthorize("hasAuthority('API:ALARM:READ-LIST-ALARMS')")
    public String search(Model model, @RequestParam Map<String, String> searchParams) {
        logger.info("#USER_LOG {},{},{},{},{}", session.getId(), session.getAttribute("username"), "search on list alarm page", "", "");
        AlarmPaginator alarmPaginator = new AlarmPaginator();
        alarmPaginator.alarmClient = alarmClient;
        alarmPaginator.alarmTypeClient = alarmTypeClient;
        alarmPaginator.deviceGroupClient = deviceGroupClient;
        alarmPaginator.parseParam(searchParams);
        alarmPaginator.loadResult(searchParams);

        loadDataSearch(alarmPaginator, model, searchParams);

        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        model.addAttribute("groupFilterList", deviceGroups);

        model.addAttribute("paginator", alarmPaginator);
        model.addAttribute("itemPerPage", searchParams.size() == 0 ? "20" : searchParams.get("limit"));
        String alertClearRemove = "";
        if (session.getAttribute("alertClearRemove") != null &&
                !session.getAttribute("alertClearRemove").equals("")) {
            alertClearRemove = (String) session.getAttribute("alertClearRemove");
            session.removeAttribute("alertClearRemove");
        }


        model.addAttribute("alertClearRemove", alertClearRemove);
        return ALARM_LIST_PAGE;
    }

    @PostMapping("/clearAlarm")
    @PreAuthorize("hasAuthority('API:ALARM:UPDATE')")
    @ResponseBody
    public String clearAlarm(Model model, @RequestParam Map<String, String> requestParams) {
        String result = "";
        if (requestParams.size() > 0) {
            try {
                if (requestParams.get("deleteIds") != null && !requestParams.get("deleteIds").isEmpty()) {
                    int cleared = 0;
                    int active = 0;
                    String[] paramAlarm = requestParams.get("deleteIds")
                            .substring(0, requestParams.get("deleteIds").length() - 1).split(",");
                    String[] paramAlarmStatus = requestParams.get("deleteStatus")
                            .substring(0, requestParams.get("deleteStatus").length() - 1).split(",");
                    for (int i = 0; i < paramAlarm.length; i++) {
                        if (!paramAlarm[i].isEmpty()) {
                            if (paramAlarmStatus[i].equals("Cleared")) {
                                cleared++;
                            } else {
                                Alarm alarm = alarmClient.get(Long.valueOf(paramAlarm[i]));
                                alarm.status = "Cleared";
                                alarmClient.update(Long.valueOf(paramAlarm[i]), alarm);
                                logger.info("#USER_LOG {},{},{},{},{}", session.getId(), session.getAttribute("username"), "clear alarm " + paramAlarm[i], "", "");
                                active++;
                            }

                        }
                    }
                    if (cleared == 0) {
                        session.setAttribute("alertClearRemove", "Clear alarms successfully!");
                    } else {
                        session.setAttribute("alertClearRemove", active + " Alarms are cleared and " + cleared + " Alarms are already cleared !");
                    }
                    session.setAttribute("action", "clear");
                    result = "successfully";
                }
            } catch (Exception e) {
                result = "fail";
            }
        }

        return result;
    }

    @PostMapping("/removeAlarm")
    @PreAuthorize("hasAuthority('API:ALARM:DELETE')")
    @ResponseBody
    public String removeAlarm(Model model, @RequestParam Map<String, String> requestParams) {

        String result = "";
        if (requestParams.size() > 0) {
            try {
                if (requestParams.get("deleteIds") != null && !requestParams.get("deleteIds").isEmpty()) {
                    String[] paramAlarm = requestParams.get("deleteIds")
                            .substring(0, requestParams.get("deleteIds").length() - 1).split(",");
                    for (int i = 0; i < paramAlarm.length; i++) {
                        if (!paramAlarm[i].isEmpty()) {
                            alarmClient.delete(Long.valueOf(paramAlarm[i]));
                            logger.info("#USER_LOG {},{},{},{},{}", session.getId(), session.getAttribute("username"), "delete alarm " + paramAlarm[i], "", "");
                        }
                    }
                    result = "successfully";
                }
            } catch (Exception e) {
                result = "fail";
            }
        }

        return result;
    }

    private void loadDataSearch(AlarmPaginator alarmPaginator, Model model, Map<String, String> searchParams) {
        if (searchParams.size() > 0) {
            if (searchParams.get(AlarmPaginator.PAGE_SERIALNUMBER) != null
                    && !searchParams.get(AlarmPaginator.PAGE_SERIALNUMBER).equals("")) {
                alarmPaginator.serialNumberSearch = searchParams.get(AlarmPaginator.PAGE_SERIALNUMBER);
            } else {
                alarmPaginator.serialNumberSearch = "";
            }
            if (searchParams.get(AlarmPaginator.PAGE_GROUP_FILTER) != null
                    && !searchParams.get(AlarmPaginator.PAGE_GROUP_FILTER).equals("")) {
                alarmPaginator.groupFilterSearch = searchParams.get(AlarmPaginator.PAGE_GROUP_FILTER);
            } else {
                alarmPaginator.groupFilterSearch = "";
            }
            if (searchParams.get(AlarmPaginator.PAGE_SEVERITY) != null
                    && !searchParams.get(AlarmPaginator.PAGE_SEVERITY).isEmpty()) {
                alarmPaginator.severitySearch = searchParams.get(AlarmPaginator.PAGE_SEVERITY);
            } else {
                alarmPaginator.severitySearch = "";
            }
            if (searchParams.get(AlarmPaginator.PAGE_ALARM_TYPE_NAME) != null
                    && !searchParams.get(AlarmPaginator.PAGE_ALARM_TYPE_NAME).isEmpty()) {
                String alarmType = searchParams.get(AlarmPaginator.PAGE_ALARM_TYPE_NAME);
                if (alarmType.equals("Request failed")) {
                    alarmType = "REQUEST_FAIL";
                } else if (alarmType == "Configuration device failed") {
                    alarmType = "CONFIGURATION_FAIL";
                } else if (alarmType == "Update firmware failed") {
                    alarmType = "UPDATE_FIRMWARE_FAIL";
                } else if (alarmType == "Reboot failed") {
                    alarmType = "REBOOT_FAIL";
                } else if (alarmType == "Factory reset failed") {
                    alarmType = "FACTORY_RESET_FAIL";
                } else {
                    alarmType = "PARAMETER_VALUE";
                }
                alarmPaginator.alarmTypeSearch = alarmType;

            } else {
                alarmPaginator.alarmTypeSearch = "";
            }
            if (searchParams.get(AlarmPaginator.PAGE_ALARM_NAME) != null
                    && !searchParams.get(AlarmPaginator.PAGE_ALARM_NAME).isEmpty()) {
                alarmPaginator.alarmNameSearch = searchParams.get(AlarmPaginator.PAGE_ALARM_NAME);
            } else {
                alarmPaginator.alarmNameSearch = "";
            }
            if (searchParams.get(AlarmPaginator.PAGE_STATUS) != null
                    && !searchParams.get(AlarmPaginator.PAGE_STATUS).isEmpty()) {
                alarmPaginator.statusSearch = searchParams.get(AlarmPaginator.PAGE_STATUS);
            } else {
                alarmPaginator.statusSearch = "";
            }
            if (searchParams.get(AlarmPaginator.PAGE_RAISED_FROM) != null
                    && !searchParams.get(AlarmPaginator.PAGE_RAISED_FROM).isEmpty()
                    && searchParams.get(AlarmPaginator.PAGE_RAISED_TO) != null
                    && !searchParams.get(AlarmPaginator.PAGE_RAISED_TO).isEmpty()
                    ) {
                alarmPaginator.raisedFromSearch = searchParams.get(AlarmPaginator.PAGE_RAISED_FROM);
                alarmPaginator.raisedToSearch = searchParams.get(AlarmPaginator.PAGE_RAISED_TO);
            } else {
                alarmPaginator.raisedFromSearch = "";
                alarmPaginator.raisedToSearch = "";
            }
            alarmPaginator.allSearch = "";
            if (searchParams.get(AlarmPaginator.PAGE_SEARCH_ALL) != null
                    && !searchParams.get(AlarmPaginator.PAGE_SEARCH_ALL).isEmpty()) {
                alarmPaginator.allSearch = searchParams.get(AlarmPaginator.PAGE_SEARCH_ALL);
                alarmPaginator.raisedFromSearch = "";
                alarmPaginator.raisedToSearch = "";
                alarmPaginator.statusSearch = "";
                alarmPaginator.alarmNameSearch = "";
                alarmPaginator.alarmTypeSearch = "";
                alarmPaginator.severitySearch = "";
                alarmPaginator.groupFilterSearch = "";
                alarmPaginator.serialNumberSearch = "";
            }

        }
    }

    private void loadData(Model model, Map<String, String> requestParams) {
        AlarmPaginator alarmPaginator = new AlarmPaginator();
        alarmPaginator.alarmClient = alarmClient;
        alarmPaginator.parseParam(requestParams);
        alarmPaginator.loadResult(requestParams);
        model.addAttribute("paginator", alarmPaginator);
        model.addAttribute("itemPerPage", requestParams.size() == 0 ? "20" : requestParams.get("limit"));
        String alertClearRemove = "";
        if (session.getAttribute("alertClearRemove") != null &&
                !session.getAttribute("alertClearRemove").equals("")) {
            alertClearRemove = (String) session.getAttribute("alertClearRemove");
            session.removeAttribute("alertClearRemove");
        }
        model.addAttribute("alertClearRemove", alertClearRemove);
        model.addAttribute("alarmNameList", "");
        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        model.addAttribute("groupFilterList", deviceGroups);

    }

}
