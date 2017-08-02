package vn.vnpt.ssdc.api.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by Lamborgini on 5/24/2017.
 */
public class Alarm extends SsdcEntity<Long> {

    //    public long deviceGroupId;
//    public String deviceGroupName;
    public String deviceId;
    public long alarmTypeId;
    public String alarmTypeName;
    public String alarmName;
    public long raised;
    public String status;
    public String description;
    public String severity;
    public Set<DeviceGroup> deviceGroups;
    public String raisedConvert;
    public long numberOfRows;
    public String deviceGroupName;

    public Alarm() {
    }

    public void setRaisedConvert(long raised) {
        Date date = new Date(raised);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.raisedConvert = df2.format(date);
    }

    public void setDeviceGroup(Set<DeviceGroup> deviceGroups) {
        String abc = "";
        for (DeviceGroup temp : deviceGroups) {
            abc += ", " + temp.name;
        }
        this.deviceGroupName = abc.substring(1);

    }
}
