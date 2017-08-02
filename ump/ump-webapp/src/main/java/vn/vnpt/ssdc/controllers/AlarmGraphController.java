package vn.vnpt.ssdc.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.vnpt.ssdc.api.client.AlarmClient;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.model.Alarm;
import vn.vnpt.ssdc.api.model.DeviceGroup;
import vn.vnpt.ssdc.models.AlarmPaginator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.toIntExact;

/**
 * Created by Lamborgini on 5/22/2017.
 */
@Controller
public class AlarmGraphController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(AlarmGraphController.class);
    private static final String ALARM_GRAPH_PAGE = "alarm_dashboard/fragments/alarm_graph";

    @Autowired
    private AlarmClient alarmClient;

    @Autowired
    private DeviceGroupClient deviceGroupClient;

    @GetMapping("/alarm-graph")
    @PreAuthorize("hasAuthority('API:ALARM:READ-LIST-ALARMS')")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to list alarm graph page","","");
        loadData(model, requestParams);
        return ALARM_GRAPH_PAGE;
    }

    @GetMapping("/alarm-graph/view-graph")
    @PreAuthorize("hasAuthority('API:ALARM:READ-LIST-ALARMS')")
    public String viewGraph(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to view alarm graph page","","");
        loadData(model, requestParams);
        return ALARM_GRAPH_PAGE;
    }

    private void loadData(Model model, Map<String, String> requestParams) {

        String groupFilter = "";
        String startTime = "";
        String endTime = "";

        if (requestParams.size() != 0) {
            groupFilter = requestParams.get("groupFilter");
            startTime = requestParams.get("startTime");
            endTime = requestParams.get("endTime");
            model.addAttribute("groupFilterSearch", groupFilter);
            model.addAttribute("startTimeSearch", startTime);
            model.addAttribute("endTimeSearch", endTime);


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                startTime = String.valueOf(sdf.parse(startTime + " 00:00:00").getTime());
                endTime = String.valueOf(sdf.parse(endTime + " 23:59:59").getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            model.addAttribute("groupFilterSearch", groupFilter);
            model.addAttribute("startTimeSearch", startTime);
            model.addAttribute("endTimeSearch", endTime);
        }

        String jsonResultSeverity = resultDataSeverityAlarm(groupFilter, startTime, endTime);
        String jsonResultNumberOfAlarmType = resultDataNumberOfAlarmType(groupFilter, startTime, endTime);
        model.addAttribute("dataSeverityAlarm", jsonResultSeverity);
        model.addAttribute("dataNumberOfAlarmType", jsonResultNumberOfAlarmType);
        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        model.addAttribute("groupFilterList", deviceGroups);
    }

    private String resultDataNumberOfAlarmType(String groupFilter, String startTime, String endTime) {

        String whereExp = "";
        if (!groupFilter.isEmpty()) {
            String groupFilterTemp = "\"name\":\"" + groupFilter + "\",";
            whereExp += " and " + AlarmPaginator.PAGE_GROUP_FILTER + " like '%" +
                    groupFilterTemp + "%'";
        }
        if (!startTime.isEmpty() && !endTime.isEmpty()) {
            whereExp += " and " + endTime + " >= raised and " + startTime + " <= raised";
        }
        if (!whereExp.isEmpty() && whereExp.startsWith(" and")) {
            whereExp = whereExp.substring(4);
        }

        Alarm[] alarmArray = alarmClient.viewGraphNumberOfAlarmType(whereExp);
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < alarmArray.length; i++) {
            List<String> listjsonObj = new ArrayList<String>();
            for (int k = 0; k < jsonArray.size(); k++) {
                JsonObject asJsonObject = jsonArray.get(k).getAsJsonObject();
                String asString = asJsonObject.get("name").getAsString();
                listjsonObj.add(asString);
            }
            if (!listjsonObj.contains(alarmArray[i].alarmName)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", alarmArray[i].alarmName +" ("+ alarmArray[i].numberOfRows +")");
                JsonArray jsonElements = new JsonArray();
                jsonElements.add(alarmArray[i].numberOfRows);
                jsonObject.add("data", jsonElements);
                jsonArray.add(jsonObject);
            }
        }

        return jsonArray.toString();

    }

    private String resultDataSeverityAlarm(String groupFilter, String startTime, String endTime) {
        String jsonPattern = "[{\"name\":\"Critical\", \"y\": %03f, \"sliced\": true, \"selected\": true}," +
                "{\"name\":\"Major\", \"y\": %03f}," +
                "{\"name\":\"Minor\", \"y\": %03f}," +
                "{\"name\":\"Warning\", \"y\": %03f}," +
                "{\"name\":\"Info\", \"y\": %03f}]";

        String whereExp = "";
        if (!groupFilter.isEmpty()) {
            String groupFilterTemp = "\"name\":\"" + groupFilter + "\",";
            whereExp += " and " + AlarmPaginator.PAGE_GROUP_FILTER + " like '%" +
                    groupFilterTemp + "%'";
        }
        if (!startTime.isEmpty() && !endTime.isEmpty()) {
            whereExp += " and " + endTime + " >= raised and " + startTime + " <= raised";
        }
        if (!whereExp.isEmpty() && whereExp.startsWith(" and")) {
            whereExp = whereExp.substring(4);
        }

        Alarm[] alarmArray = alarmClient.viewGraphSeverityAlarm(whereExp);
        double critical = 0;
        double info = 0;
        double minor = 0;
        double major = 0;
        double warning = 0;
        if (alarmArray.length > 0) {
            for (int i = 0; i < alarmArray.length; i++) {
                if (alarmArray[i].severity.equals("Critical")) {
                    critical = alarmArray[i].numberOfRows;
                } else if (alarmArray[i].severity.equals("Info")) {
                    info = alarmArray[i].numberOfRows;
                } else if (alarmArray[i].severity.equals("Major")) {
                    major = alarmArray[i].numberOfRows;
                } else if (alarmArray[i].severity.equals("Warning")) {
                    warning = alarmArray[i].numberOfRows;
                } else if (alarmArray[i].severity.equals("Minor")) {
                    minor = alarmArray[i].numberOfRows;
                }
            }

            double total = critical + info + major + minor + warning;
            critical = (critical / total) * 100;
            info = (info / total) * 100;
            major = (major / total) * 100;
            warning = (warning / total) * 100;
            minor = (minor / total) * 100;
        }

        return String.format(jsonPattern, critical, major, minor, warning, info);
    }
}

