package vn.vnpt.ssdc.api.model;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by vietnq on 11/11/16.
 */
public class DeviceTypeVersion extends SsdcEntity<Long> {
    public Long deviceTypeId;
    public String firmwareVersion;
    public Map<String, Parameter> parameters;
    public String dataModelFileName;
    public String firmwareFileName;
    public String firmwareFileId;
    public String manufacturer;
    public String oui;
    public String productClass;
    public String modelName;
    public String createDate;
    public String exportUrl;
    public String exportJsonUrl;

    public DeviceTypeVersion() {
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Long getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Long deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getOui() {
        return oui;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }

    public String getProductClass() {
        return productClass;
    }

    public void setProductClass(String productClass) {
        this.productClass = productClass;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getDataModelFileName() {
        return dataModelFileName;
    }

    public void setDataModelFileName(String dataModelFileName) {
        this.dataModelFileName = dataModelFileName;
    }

    public String getFirmwareFileName() {
        return firmwareFileName;
    }

    public void setFirmwareFileName(String firmwareFileName) {
        this.firmwareFileName = firmwareFileName;
    }

    public String getFirmwareFileId() {
        return firmwareFileId;
    }

    public void setFirmwareFileId(String firmwareFileId) {
        this.firmwareFileId = firmwareFileId;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public String getExportUrl() {
        return exportUrl;
    }

    public void setExportUrl(String exportUrl) {
        this.exportUrl = exportUrl;
    }

    public String getExportJsonUrl() {
        return exportJsonUrl;
    }

    public void setExportJsonUrl(String exportJsonUrl) {
        this.exportJsonUrl = exportJsonUrl;
    }

    public String convertCreatedMilliSecondToStringDate() {
        String result = "";
        if (created != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result = sdf.format(created);
        }
        return result;
    }

    public String convertUpdateMilliSecondToStringDate() {
        String result = "";
        if (updated != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result = sdf.format(updated);
        }
        return result;
    }
}
