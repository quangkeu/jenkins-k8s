package vn.vnpt.ssdc.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.model.DeviceGroup;


import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by thangnc on 10-Apr-17.
 */
public class DeviceGroupPaginator  extends Paginator<DeviceGroup> {

    public static final Logger logger = LoggerFactory.getLogger(DeviceGroupPaginator.class);

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_CURRENT = "indexPage";
    public int currentPage;
    public int lastPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public int offset = 1;
    public Map<String, String> indexParams = new HashMap<String, String>();
    public String description;
    public List<DeviceGroup> deviceGroupList;

    public DeviceGroupPaginator() {
        this.currentPage = 0;
    }

    public DeviceGroupClient deviceGroupClient;

    public void loadResult(Map<String, String> requestParams) {
        if(requestParams.get(PAGE_CURRENT) != null && !("").equals(requestParams.get(PAGE_CURRENT)))  offset = Integer.parseInt(requestParams.get(PAGE_CURRENT));
        if(requestParams.get(PAGE_SIZE) != null && !("").equals(requestParams.get(PAGE_SIZE)))  pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));

        DeviceGroup[] deviceGroups = deviceGroupClient.findByPage(offset, pageSize);

        List<DeviceGroup> deviceGroupList = new LinkedList<>();

        for (int i = 0; i < deviceGroups.length; i++) {
            try {
                DeviceGroup deviceGroup = deviceGroups[i];
                deviceGroup.parameters = new HashMap<String, String>() {{
                    put("Id", deviceGroup.getId() + "");
                    put("Name", deviceGroup.name);
                    put("Manufacturer", deviceGroup.manufacturer);
                    put("Model name", deviceGroup.modelName);
                    put("Firmware/Software version", deviceGroup.firmwareVersion);
                    put("Label", deviceGroup.label);
                }};
                deviceGroupList.add(deviceGroup);
            } catch (Exception e) {}
        }

        this.deviceGroupList = deviceGroupList;
        this.totalPages = deviceGroupClient.findAll().length;

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
