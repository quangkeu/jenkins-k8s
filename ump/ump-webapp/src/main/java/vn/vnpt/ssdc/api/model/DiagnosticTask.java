package vn.vnpt.ssdc.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thangnc on 24-Feb-17.
 */
public class DiagnosticTask extends SsdcEntity<Long>{
    public String deviceId;
    public String diagnosticsName;
    public Map<String, Parameter> parameterFull;
    public Map<String, Object> request;
    public Map<String, String> result;
    public Long status;
    //Time Done
    public Long completed;
    public String taskId;

    public Map<String,String> parameters = new HashMap<String,String>();

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDiagnosticsName() {
        return diagnosticsName;
    }

    public void setDiagnosticsName(String diagnosticsName) {
        this.diagnosticsName = diagnosticsName;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getCompleted() {
        return completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Map<String, Parameter> getParameterFull() {
        return parameterFull;
    }

    public void setParameterFull(Map<String, Parameter> parameterFull) {
        this.parameterFull = parameterFull;
    }

    public Map<String, Object> getRequest() {
        return request;
    }

    public void setRequest(Map<String, Object> request) {
        this.request = request;
    }

    public Map<String, String> getResult() {
        return result;
    }

    public void setResult(Map<String, String> result) {
        this.result = result;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
