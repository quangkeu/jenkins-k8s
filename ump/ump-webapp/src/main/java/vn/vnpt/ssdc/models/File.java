package vn.vnpt.ssdc.models;

/**
 * Created by Lamborgini on 3/17/2017.
 */
public class File {
    private String oui;
    private String productClass;
    private String version;

    public File() {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
