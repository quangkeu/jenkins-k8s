package vn.vnpt.ssdc.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyJob extends SsdcEntity<Long> {
    public String name;
    public String status;
    //INIT, EXCUTE, STOP
    public Long deviceGroupId;
    public List<String> externalDevices;
    public String externalFilename;
    public Long startAt;
    public Integer timeInterval;
    public Integer maxNumber;
    public List<String> events;
    public Boolean isImmediately;
    public String actionName;
    public Map<String, Object> parameters;
    public Integer limited;
    public Integer priority;

    private Long numberOfExecutions;
    private String deviceGroupName;

    public Long getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(Long numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    public String getDeviceGroupName() {
        return deviceGroupName;
    }

    public void setDeviceGroupName(String deviceGroupName) {
        this.deviceGroupName = deviceGroupName;
    }

    public PolicyJob() {
        this.externalDevices = new ArrayList<String>();
        this.events = new ArrayList<String>();
        this.parameters = new HashMap<String, Object>();
    }
}
