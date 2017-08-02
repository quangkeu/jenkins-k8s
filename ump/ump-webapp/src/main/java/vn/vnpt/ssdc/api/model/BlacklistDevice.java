package vn.vnpt.ssdc.api.model;

/**
 * Created by Lamborgini on 3/3/2017.
 */
public class BlacklistDevice extends SsdcEntity<Long>{
    public String deviceId;

    public BlacklistDevice() {
    }

    public BlacklistDevice(String deviceID) {
        this.deviceId = deviceID;
    }

    public String getDeviceID() {
        return deviceId;
    }

    public void setDeviceID(String deviceID) {
        this.deviceId = deviceID;
    }
}
