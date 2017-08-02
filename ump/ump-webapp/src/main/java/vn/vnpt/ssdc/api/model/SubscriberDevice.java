package vn.vnpt.ssdc.api.model;

public class SubscriberDevice extends SsdcEntity<Long> implements Cloneable {

    public String subscriberId;
    public String deviceId;
    public String manufacturer;
    public String oui;
    public String productClass;
    public String serialNumber;
    private String modelName;

    public void generateDeviceId() {

        // Create device id from oui, productClass, serialNumber.
        this.deviceId = oui + "-" + productClass + "-" + serialNumber;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
