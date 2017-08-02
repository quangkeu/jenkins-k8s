package vn.vnpt.ssdc.api.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class SubscriberTemplate extends SsdcEntity<Long> {

    public String name;
    public Set<String> templateKeys;

    public SubscriberTemplate() {
        templateKeys = new LinkedHashSet<>();
    }

}