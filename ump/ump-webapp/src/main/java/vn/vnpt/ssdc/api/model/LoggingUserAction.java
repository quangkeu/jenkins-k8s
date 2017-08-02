package vn.vnpt.ssdc.api.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LoggingUserAction {
    public String action;
    public String time;
    public String affected;
    public String taskId;
    public List<LoggingDevice> loggingDevices;

    public LoggingUserAction() {
        this.loggingDevices = new LinkedList<LoggingDevice>();
    }

}