package vn.vnpt.ssdc.api.model;

/**
 * Created by THANHLX on 4/17/2017.
 */
public class PolicyTask extends SsdcEntity<Long> {
    public String deviceId;
    public Long policyJobId;
    public Integer status;//1: SUCCESS - 2: FAIL
    public Long completed;
    public String taskId;
    public String errorCode;
    public String errorText;

    private String oui;
    private String productClass;
    private String serialNumber;

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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
