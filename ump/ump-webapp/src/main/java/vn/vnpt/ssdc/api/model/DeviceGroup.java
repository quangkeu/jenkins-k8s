package vn.vnpt.ssdc.api.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by thangnc on 06-Feb-17.
 */
public class DeviceGroup extends SsdcEntity<Long> {
    public String name;
    public Map<String, Filter> filters;
    public String query;
    public String manufacturer;
    public String modelName;
    public String firmwareVersion;
    public String label;
    public String oui;
    public String productClass;
    public Map<String,String> parameters = new HashMap<String,String>();

}

