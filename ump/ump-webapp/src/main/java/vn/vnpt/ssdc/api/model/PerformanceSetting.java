package vn.vnpt.ssdc.api.model;

import java.util.List;

/**
 * Created by thangnc on 21-Jun-17.
 */
public class PerformanceSetting extends SsdcEntity<Long> {

    public String stasticsType;
    public String type;
    public Integer stasticsInterval;
    public Integer monitoring;
    public String deviceId;
    public Long deviceGroupId;
    public List<String> externalDevices;
    public String external_filename;
    public Long start;
    public Long end;

    public String manufacturer;
    public String modelName;
    public String serialNumber;

    public String textMonitoring;
    public String textFromDate;
    public String textToDate;
    public List<String> parameterNames;

}