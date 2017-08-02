package vn.vnpt.ssdc.api.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Provisioning extends SsdcEntity<Long> {

    public DeviceTypeVersion deviceTypeVersion;
    public Tag tag;
    public Set<DeviceTypeVersion> deviceTypeVersions;
    public Set<Tag> tags;

    public Provisioning() {
        this.deviceTypeVersion = new DeviceTypeVersion();
        this.deviceTypeVersions = new LinkedHashSet<DeviceTypeVersion>();
        this.tag = new Tag();
        this.tags = new LinkedHashSet<Tag>();
    }

    public Provisioning(DeviceTypeVersion deviceTypeVersion, Set<Tag> tags) {
        this.deviceTypeVersion = deviceTypeVersion;
        this.tags = tags;
    }

    public Provisioning(Tag tag, Set<DeviceTypeVersion> deviceTypeVersions) {
        this.tag = tag;
        this.deviceTypeVersions = deviceTypeVersions;
    }
}
