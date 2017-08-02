package vn.vnpt.ssdc.api.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Subscriber extends SsdcEntity<Long> {

    public String subscriberId;
    public Set<String> subscriberDataTemplateIds;
    public Map<String,String> subscriberData;

    public Subscriber() {
        subscriberData = new LinkedHashMap<String, String>();
        subscriberDataTemplateIds = new LinkedHashSet<String>();
    }

}
