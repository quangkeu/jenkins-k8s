package vn.vnpt.ssdc.api.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by thangnc on 23-May-17.
 */
public class AlarmType extends SsdcEntity<Long> {
    public String type;
    public String name;
    public Set<DeviceGroup> deviceGroups;
    public String severity;
    public Boolean notify;
    public Boolean monitor;
    public long aggregatedVolume;
    public String notifyAggregated;
    public Map<String, String> parameterValues;

    public Map<String,String> parameters = new HashMap<String,String>();
    public String deviceGroupsName;
    public String deviceGroupsId;

    public Integer notification;
    public Integer timeSettings;
}
