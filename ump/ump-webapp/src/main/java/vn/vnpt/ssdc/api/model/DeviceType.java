package vn.vnpt.ssdc.api.model;

import java.util.Map;

/**
 * Created by vietnq on 11/2/16.
 */
public class DeviceType extends SsdcEntity<Long> {
    public String name;
    public String manufacturer;
    public String oui;
    public String productClass;
    public String firmwareVersion;
    public Map<String,Parameter> parameters;
}