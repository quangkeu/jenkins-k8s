package vn.vnpt.ssdc.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.api.client.AlarmTypeClient;
import vn.vnpt.ssdc.api.model.AlarmType;
import vn.vnpt.ssdc.api.model.DeviceGroup;

import java.util.*;

/**
 * Created by thangnc on 25-May-17.
 */
public class AlarmSettingPaginator extends Paginator<AlarmType>{

    public static final Logger logger = LoggerFactory.getLogger(AlarmSettingPaginator.class);

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_CURRENT = "indexPage";

    public int currentPage;
    public int lastPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public int offset = 1;
    public Map<String, String> indexParams = new HashMap<String, String>();

    public AlarmType[] alarmTypes;
    public String description;

    public AlarmSettingPaginator() {
        this.currentPage = 0;
    }

    public AlarmTypeClient alarmTypeClient;

    public void loadResult(Map<String, String> requestParams) {
        if(requestParams.get(PAGE_CURRENT) != null && !("").equals(requestParams.get(PAGE_CURRENT)))  offset = Integer.parseInt(requestParams.get(PAGE_CURRENT));
        if(requestParams.get(PAGE_SIZE) != null && !("").equals(requestParams.get(PAGE_SIZE)))  pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));

        this.totalPages = alarmTypeClient.findAll().length;
        this.alarmTypes = alarmTypeClient.getAlarmByPage(offset, pageSize);
        for(int i = 0; i < this.alarmTypes.length; i++) {
            String name = "";
            String id = "";
            Iterator<DeviceGroup> deviceGroups = this.alarmTypes[i].deviceGroups.iterator();
            while (deviceGroups.hasNext()) {
                DeviceGroup deviceGroup = deviceGroups.next();
                name = name + deviceGroup.name + ", ";
                id = id + deviceGroup.id + ",";
            }
            this.alarmTypes[i].deviceGroupsName = name.substring(0, name.length() - 2);
            this.alarmTypes[i].deviceGroupsId = id.substring(0, id.length() - 1);
        }
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

    public void loadResultSearch(Map<String, String> requestParams) {
        String type = "";
        String alarmName = "";
        String severity = "";
        String group = "";
        String prefix = "";
        if(!("").equals(requestParams.get("alarmType"))) type = requestParams.get("alarmType");
        if(!("").equals(requestParams.get("alarmName"))) alarmName = requestParams.get("alarmName");
        if(!("").equals(requestParams.get("severity"))) severity = requestParams.get("severity");
        if(!("").equals(requestParams.get("group"))) group = requestParams.get("group");
        if(!("").equals(requestParams.get("textSearch"))) prefix = requestParams.get("textSearch");
        this.alarmTypes = alarmTypeClient.search(type, alarmName, severity, group, prefix);
        for(int i = 0; i < this.alarmTypes.length; i++) {
            String name = "";
            String id = "";
            Iterator<DeviceGroup> deviceGroups = this.alarmTypes[i].deviceGroups.iterator();
            while (deviceGroups.hasNext()) {
                DeviceGroup deviceGroup = deviceGroups.next();
                name = name + deviceGroup.name + ", ";
                id = id + deviceGroup.id + ",";
            }
            this.alarmTypes[i].deviceGroupsName = name.substring(0, name.length() - 2);
            this.alarmTypes[i].deviceGroupsId = id.substring(0, id.length() - 1);
        }
        this.totalPages = this.alarmTypes.length;
    }

    public void parseParam(HashMap<String, String> requestParams) {
        this.number++;
        if (requestParams != null && !requestParams.isEmpty()) {
            if (isExits(requestParams, PAGE_CURRENT)) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
                this.number = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (isExits(requestParams, PAGE_SIZE) && !("0").equals(requestParams.get(PAGE_SIZE))) {
                this.pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));
            }
        }
    }

    public static boolean isExits(HashMap<String, String> requestParams, String key) {
        if (requestParams.containsKey(key) && requestParams.get(key) != null) {
            return true;
        }
        return false;
    }
}
