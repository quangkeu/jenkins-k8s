package vn.vnpt.ssdc.models;

import vn.vnpt.ssdc.api.client.AlarmClient;
import vn.vnpt.ssdc.api.client.AlarmTypeClient;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.model.Alarm;
import vn.vnpt.ssdc.api.model.AlarmType;
import vn.vnpt.ssdc.api.model.DeviceGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lamborgini on 5/24/2017.
 */
public class AlarmPaginator extends Paginator<Alarm> {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_QUERY = "query";
    public static final String PAGE_OFFSET = "offset";
    public static final String PAGE_PARAM = "parameters";
    public static final String PAGE_CURRENT = "indexPage";
    public Map<String, String> alarmQueryParam = new HashMap<>();
    public Map<String, String> indexParams = new HashMap<>();
    public int lastPage;
    public int currentPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public AlarmClient alarmClient;
    public AlarmTypeClient alarmTypeClient;
    public DeviceGroupClient deviceGroupClient;
    public List<Alarm> alarmList = new ArrayList<Alarm>();
    public List<String> alarmNameList = new ArrayList<String>();
    public DeviceGroup[] groupFilterList;

    public static final String PAGE_SERIALNUMBER = "device_id";
    public static final String PAGE_SEVERITY = "severity";
    public static final String PAGE_ALARM_TYPE_NAME = "alarm_type_name";
    public static final String PAGE_ALARM_NAME = "alarm_name";
    public static final String PAGE_STATUS = "status";
    public static final String PAGE_GROUP_FILTER = "device_groups";
    public static final String PAGE_RAISED_FROM = "raised_from";
    public static final String PAGE_RAISED_TO = "raised_to";
    public static final String PAGE_SEARCH_ALL = "search_all";
    public static final String PAGE_DESCRIPTION = "description";
    public String whereExp = "";

    public String serialNumberSearch = "";
    public String groupFilterSearch = "";
    public String severitySearch = "";
    public String alarmTypeSearch = "";
    public String statusSearch = "";
    public String alarmNameSearch = "";
    public String raisedFromSearch = "";
    public String raisedToSearch = "";
    public String allSearch = "";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public AlarmPaginator() {
        this.currentPage = 1;
    }

    public void parseParam(Map<String, String> requestParams) {
        String queryInput = null;
        this.number++;
        if (requestParams != null && !requestParams.isEmpty()) {
            if (requestParams.containsKey(PAGE_CURRENT) && requestParams.get(PAGE_CURRENT) != null) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
                this.number = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (requestParams.containsKey(PAGE_SIZE) && requestParams.get(PAGE_SIZE) != null) {
                if (!requestParams.get(PAGE_SIZE).toString().equals("0"))
                    this.pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));
            }
            if (requestParams.containsKey(PAGE_QUERY) && requestParams.get(PAGE_QUERY) != null) {
                queryInput = requestParams.get(PAGE_QUERY).toString();
            }
        }
        this.alarmQueryParam.put(PAGE_SIZE, String.valueOf(this.pageSize));
        if (queryInput != null) {
            this.alarmQueryParam.put(PAGE_QUERY, queryInput);
        }
        this.alarmQueryParam.put(PAGE_CURRENT, currentPage != 0 ? String.valueOf(currentPage - 1) : "0");


        if (requestParams.get(AlarmPaginator.PAGE_SERIALNUMBER) != null
                && !requestParams.get(AlarmPaginator.PAGE_SERIALNUMBER).isEmpty()) {
            whereExp += " and " + AlarmPaginator.PAGE_SERIALNUMBER + " like '%" + requestParams.get(AlarmPaginator.PAGE_SERIALNUMBER) + "%'";
        }
        if (requestParams.get(AlarmPaginator.PAGE_SEVERITY) != null
                && !requestParams.get(AlarmPaginator.PAGE_SEVERITY).isEmpty()) {
            whereExp += " and " + AlarmPaginator.PAGE_SEVERITY + " = '" + requestParams.get(AlarmPaginator.PAGE_SEVERITY) + "'";
        }
        if (requestParams.get(AlarmPaginator.PAGE_ALARM_TYPE_NAME) != null
                && !requestParams.get(AlarmPaginator.PAGE_ALARM_TYPE_NAME).isEmpty()) {
            String alarmTypeParam = requestParams.get(AlarmPaginator.PAGE_ALARM_TYPE_NAME);
            AlarmType[] alarmTypes = alarmTypeClient.search(alarmTypeParam, "", "", "", "");
            for (int i = 0; i < alarmTypes.length; i++) {
                alarmNameList.add(alarmTypes[i].name);
            }
            if (alarmTypeParam.equals("PARAMETER_VALUE")) {
                whereExp += " and not " + AlarmPaginator.PAGE_ALARM_TYPE_NAME + " = 'Request failed'"
                        + " and not " + AlarmPaginator.PAGE_ALARM_TYPE_NAME + " = 'Configuration device failed'"
                        + " and not " + AlarmPaginator.PAGE_ALARM_TYPE_NAME + " = 'Update firmware failed'"
                        + " and not " + AlarmPaginator.PAGE_ALARM_TYPE_NAME + " = 'Reboot failed'"
                        + " and not " + AlarmPaginator.PAGE_ALARM_TYPE_NAME + " = 'Factory reset failed'";
            } else {
                whereExp += " and " + AlarmPaginator.PAGE_ALARM_TYPE_NAME + " = '" +
                        alarmTypeParam + "'";
            }
//            Alarm[] alarmNameByAlarmType = alarmClient.getAlarmNameByAlarmType(requestParams.get(AlarmPaginator.PAGE_ALARM_TYPE_NAME));
//            for(Alarm alarm : alarmNameByAlarmType){
//                alarmNameList.add(alarm.alarmName);
//            }
        }
        if (requestParams.get(AlarmPaginator.PAGE_ALARM_NAME) != null
                && !requestParams.get(AlarmPaginator.PAGE_ALARM_NAME).isEmpty()) {
            whereExp += " and " + AlarmPaginator.PAGE_ALARM_NAME + " = '" +
                    requestParams.get(AlarmPaginator.PAGE_ALARM_NAME) + "'";
        }
        if (requestParams.get(AlarmPaginator.PAGE_STATUS) != null
                && !requestParams.get(AlarmPaginator.PAGE_STATUS).isEmpty()) {
            whereExp += " and " + AlarmPaginator.PAGE_STATUS + " = '" + requestParams.get(AlarmPaginator.PAGE_STATUS) + "'";
        }
        if (requestParams.get(AlarmPaginator.PAGE_GROUP_FILTER) != null
                && !requestParams.get(AlarmPaginator.PAGE_GROUP_FILTER).isEmpty()) {
            String groupFilter = "\"name\":\"" + requestParams.get(AlarmPaginator.PAGE_GROUP_FILTER) + "\",";
            whereExp += " and " + AlarmPaginator.PAGE_GROUP_FILTER + " like '%" +
                    groupFilter + "%'";
        }

        if (requestParams.get(AlarmPaginator.PAGE_RAISED_FROM) != null
                && !requestParams.get(AlarmPaginator.PAGE_RAISED_FROM).isEmpty()
                && requestParams.get(AlarmPaginator.PAGE_RAISED_TO) != null
                && !requestParams.get(AlarmPaginator.PAGE_RAISED_TO).isEmpty()
                ) {
            String startTime = "";
            String endTime = "";
            try {
                startTime = String.valueOf(sdf.parse(requestParams.get(AlarmPaginator.PAGE_RAISED_FROM)).getTime());
                endTime = String.valueOf(sdf.parse(requestParams.get(AlarmPaginator.PAGE_RAISED_TO)).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            whereExp += " and " + endTime + " >= raised and " + startTime + " <= raised";
        }

        if (requestParams.get(AlarmPaginator.PAGE_SEARCH_ALL) != null
                && !requestParams.get(AlarmPaginator.PAGE_SEARCH_ALL).isEmpty()) {
            String param = requestParams.get(AlarmPaginator.PAGE_SEARCH_ALL);
            whereExp += " and " + AlarmPaginator.PAGE_SERIALNUMBER + " like '%" + param + "%'"
                    + " or " + AlarmPaginator.PAGE_ALARM_TYPE_NAME + " like '%" + param + "%'"
                    + " or " + AlarmPaginator.PAGE_SEVERITY + " like '%" + param + "%'"
                    + " or " + AlarmPaginator.PAGE_GROUP_FILTER + " like '%" + param + "%'"
                    + " or " + AlarmPaginator.PAGE_STATUS + " like '%" + param + "%'"
                    + " or " + AlarmPaginator.PAGE_DESCRIPTION + " like '%" + param + "%'";
        }

        if (!whereExp.isEmpty() && whereExp.startsWith(" and")) {
            whereExp = whereExp.substring(4);
        }
    }

    public void loadResult(Map<String, String> requestParams) {

        Alarm[] alarms = alarmClient.findAlamTypePage(alarmQueryParam, whereExp);
        if (alarms.length > 0) {
            for (Alarm alarm : alarms) {
                alarm.setRaisedConvert(alarm.raised);
                alarm.setDeviceGroup(alarm.deviceGroups);
                alarmList.add(alarm);
            }
        }
        this.totalPages = alarmClient.countAlarmPage(whereExp);

        if (this.totalPages == 0) {
            this.lastPage = 1;
        } else {
            int page = this.totalPages % (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            int pageTotal = this.totalPages / (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            this.lastPage = page == 0 ? pageTotal : pageTotal + 1;
        }
    }
}
