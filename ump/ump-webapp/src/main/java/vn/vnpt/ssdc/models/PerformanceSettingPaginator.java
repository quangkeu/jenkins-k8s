package vn.vnpt.ssdc.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.api.client.AlarmTypeClient;
import vn.vnpt.ssdc.api.client.PerformanceSettingApiClient;
import vn.vnpt.ssdc.api.model.AlarmType;
import vn.vnpt.ssdc.api.model.DeviceGroup;
import vn.vnpt.ssdc.api.model.PerformanceSetting;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by thangnc on 25-May-17.
 */
public class PerformanceSettingPaginator extends Paginator<PerformanceSetting>{

    public static final Logger logger = LoggerFactory.getLogger(PerformanceSettingPaginator.class);

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_CURRENT = "indexPage";

    public int currentPage;
    public int lastPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public int offset = 1;
    public Map<String, String> indexParams = new HashMap<String, String>();

    public PerformanceSetting[] performanceSettings;
    public String description;

    public PerformanceSettingPaginator() {
        this.currentPage = 0;
    }

    public PerformanceSettingApiClient performanceSettingApiClient;

    public void loadResult(Map<String, String> requestParams) {
        if(requestParams.get(PAGE_CURRENT) != null && !("").equals(requestParams.get(PAGE_CURRENT)))  offset = Integer.parseInt(requestParams.get(PAGE_CURRENT));
        if(requestParams.get(PAGE_SIZE) != null && !("").equals(requestParams.get(PAGE_SIZE)))  pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));

        this.totalPages = performanceSettingApiClient.findAll().length;
        this.performanceSettings = performanceSettingApiClient.getPerformanceByPage(offset, pageSize);
        for(int i = 0; i < this.performanceSettings.length; i++) {
            this.performanceSettings[i].textFromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.performanceSettings[i].start));
            this.performanceSettings[i].textToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.performanceSettings[i].end));
            if(this.performanceSettings[i].monitoring == 1) {
                this.performanceSettings[i].textMonitoring = "Single CPE";
            }
            if(this.performanceSettings[i].monitoring == 2) {
                this.performanceSettings[i].textMonitoring = "By group filter";
            }
            if(this.performanceSettings[i].monitoring == 3) {
                this.performanceSettings[i].textMonitoring = "By external file";
            }
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
        String traffic = "";
        String monitoring = "";
        String fromDate = "";
        String toDate = "";
        String prefix = "";
        if(!("").equals(requestParams.get("traffic"))) traffic = requestParams.get("traffic");
        if(!("").equals(requestParams.get("monitoring"))) monitoring = requestParams.get("monitoring");
        if(!("").equals(requestParams.get("fromDate"))) fromDate = requestParams.get("fromDate");
        if(!("").equals(requestParams.get("toDate"))) toDate = requestParams.get("toDate");
        if(!("").equals(requestParams.get("textSearch"))) prefix = requestParams.get("textSearch");
        this.performanceSettings = performanceSettingApiClient.search(traffic, monitoring, fromDate, toDate, prefix);
        for(int i = 0; i < this.performanceSettings.length; i++) {
            this.performanceSettings[i].textFromDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(this.performanceSettings[i].start));
            this.performanceSettings[i].textToDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(this.performanceSettings[i].end));
            if(this.performanceSettings[i].monitoring == 1) {
                this.performanceSettings[i].textMonitoring = "Single CPE";
            }
            if(this.performanceSettings[i].monitoring == 2) {
                this.performanceSettings[i].textMonitoring = "By group filter";
            }
            if(this.performanceSettings[i].monitoring == 3) {
                this.performanceSettings[i].textMonitoring = "By external file";
            }
        }
        this.totalPages = this.performanceSettings.length;
    }

    public void parseParam(HashMap<String, String> requestParams) {
        this.number++;
        if (requestParams != null && !requestParams.isEmpty()) {
            if (requestParams.containsKey(PAGE_CURRENT) && requestParams.get(PAGE_CURRENT) != null) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
                this.number = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (requestParams.containsKey(PAGE_SIZE) && !("0").equals(requestParams.get(PAGE_SIZE))) {
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
