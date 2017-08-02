package vn.vnpt.ssdc.api.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoggingDevice extends SsdcEntity<Long> {
    public String session;
    public String deviceId;
    public String time;
    public String status;
    public Map<Integer, CwmpLoggingDevice> cwmps;

    public LoggingDevice() {
        this.cwmps = new LinkedHashMap<Integer, CwmpLoggingDevice>();
    }

}
