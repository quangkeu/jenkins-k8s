package vn.vnpt.ssdc.api.model;

import com.google.gson.Gson;

/**
 * Created by vietnq on 11/2/16.
 */
public class DeviceType extends SsdcEntity<Long> {
    public String name;
    public String manufacturer;
    public String oui;
    public String productClass;
    public String connectionRequestUsername;
    public String connectionRequestPassword;
    public Integer intervalTime;

    public String firmwareVersion;
    public String createDate;

    public DeviceType() {

    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getConnectionRequestUsername() {
        return connectionRequestUsername;
    }

    public void setConnectionRequestUsername(String connectionRequestUsername) {
        this.connectionRequestUsername = connectionRequestUsername;
    }

    public String getConnectionRequestPassword() {
        return connectionRequestPassword;
    }

    public void setConnectionRequestPassword(String connectionRequestPassword) {
        this.connectionRequestPassword = connectionRequestPassword;
    }
    public void setId(Long id) {
         this.id = id;
    }
    public Long getId(){
        return this.id;
    }

    public Integer getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Integer intervalTime) {
        this.intervalTime = intervalTime;
    }
}