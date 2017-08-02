package vn.vnpt.ssdc.api.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A tag is a collection of parameters <br/>
 * It is assigned to device type for dynamic configuration </br>
 * For example: VoIP tag, WAN tag, Device summary tag, ....
 * <p>
 * Created by vietnq on 11/1/16.
 */
public class Tag extends SsdcEntity<Long>  {
    public String name;
    public Map<String, Parameter> parameters;
    public Long deviceTypeVersionId;
    //0 : unassigned, 1 : assigned, use integer for cross-platform db
    public Integer assigned;
    public String assignedGroup;
    public String parentObject;
    public Long rootTagId;
    public Integer synchronize; //0 : off auto synchronize, 1 : on auto synchronize

    public Map<String, HashMap<String, String>> parametersObject;

    private Boolean resultSearch;

    public Tag() {
        assigned = 0;
        synchronize = 0;
        parameters = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    public Long getDeviceTypeVersionId() {
        return deviceTypeVersionId;
    }

    public void setDeviceTypeVersionId(Long deviceTypeVersionId) {
        this.deviceTypeVersionId = deviceTypeVersionId;
    }

    public Integer getAssigned() {
        return assigned;
    }

    public void setAssigned(Integer assigned) {
        this.assigned = assigned;
    }

    public String getAssignedGroup() {
        return assignedGroup;
    }

    public void setAssignedGroup(String assignedGroup) {
        this.assignedGroup = assignedGroup;
    }

    public String getParentObject() {
        return parentObject;
    }

    public void setParentObject(String parentObject) {
        this.parentObject = parentObject;
    }

    public Long getRootTagId() {
        return rootTagId;
    }

    public void setRootTagId(Long rootTagId) {
        this.rootTagId = rootTagId;
    }

    public Integer getSynchronize() {
        return synchronize;
    }

    public void setSynchronize(Integer synchronize) {
        this.synchronize = synchronize;
    }

    public Map<String, HashMap<String, String>> getParametersObject() {
        return parametersObject;
    }

    public void setParametersObject(Map<String, HashMap<String, String>> parametersObject) {
        this.parametersObject = parametersObject;
    }

    public Boolean getResultSearch() {
        return resultSearch;
    }

    public void setResultSearch(Boolean resultSearch) {
        this.resultSearch = resultSearch;
    }
}
