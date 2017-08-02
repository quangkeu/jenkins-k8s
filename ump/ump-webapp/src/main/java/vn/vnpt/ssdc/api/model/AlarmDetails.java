package vn.vnpt.ssdc.api.model;

import java.util.Set;

/**
 * Created by thangnc on 01-Aug-17.
 */
public class AlarmDetails extends SsdcEntity<Long> {
    public long alarm_type_id;
    public String alarm_type;
    public String alarm_type_name;
    public String device_id;
    public Set<DeviceGroup> deviceGroups;
}
