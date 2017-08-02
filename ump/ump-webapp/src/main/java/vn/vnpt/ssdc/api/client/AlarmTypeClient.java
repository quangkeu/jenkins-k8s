package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.AlarmType;

/**
 * Created by thangnc on 23-May-17.
 */
@Component
public class AlarmTypeClient extends GenericApiClient<Long, AlarmType>{

    @Autowired
    public AlarmTypeClient(RestTemplate restTemplate,
                             @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = AlarmType.class;
        this.endpointUrl = apiEndpointUrl + "/alarm-type";
    }

    public AlarmType[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, AlarmType[].class);
    }

    public AlarmType[] getAlarmByPage(int offset, int limit) {
        String url = String.format("%s/getAlarmByPage", this.endpointUrl);
        url += "?offset=" + offset + "&limit=" + limit;
        return this.restTemplate.getForObject(url, AlarmType[].class);
    }

    public AlarmType[] getAlarmByName(String name) {
        String url = String.format("%s/getAlarmByName", this.endpointUrl);
        url += "?name=" + name;
        return this.restTemplate.getForObject(url, AlarmType[].class);
    }



    public AlarmType[] search(String type, String name, String severity, String group, String prefix) {
        String url = String.format("%s/search", this.endpointUrl);
        url += "?type=" + type + "&name=" + name + "&severity=" + severity + "&group=" + group + "&prefix=" + prefix;
        return this.restTemplate.getForObject(url, AlarmType[].class);
    }

    public AlarmType[] findAlarmTypeByGroupID(String groupId) {
        return this.restTemplate.getForObject(String.format("%s/get-alarm-type-by-group?groupId=%s",
                this.endpointUrl, groupId), AlarmType[].class);
    }

}
