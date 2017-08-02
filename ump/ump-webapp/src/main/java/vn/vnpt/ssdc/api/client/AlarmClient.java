package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Alarm;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Lamborgini on 5/24/2017.
 */
@Component
public class AlarmClient extends GenericApiClient<Long, Alarm>{

    @Autowired
    public AlarmClient(RestTemplate restTemplate,
                           @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Alarm.class;
        this.endpointUrl = apiEndpointUrl + "/alarm";
    }

    @Autowired
    private HttpSession session;

    public Alarm[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, Alarm[].class);
    }

    public Alarm[] findAlamTypePage(Map<String, String> alarmTypeQueryParam, String whereExp) {
        String rolesStr = session.getAttribute("deviceGroupIds").toString();
        rolesStr = rolesStr.replaceAll("[\"\\[\\]]", "");

        String url = String.format("%s/search-alarm?limit=%s&indexPage=%s&whereExp=%s&deviceGroupIds=%s", this.endpointUrl,
                alarmTypeQueryParam.get("limit"),alarmTypeQueryParam.get("indexPage"), whereExp, rolesStr);
        return this.restTemplate.getForObject(url,Alarm[].class);
    }

    public int countAlarmPage(String whereExp) {
        String rolesStr = session.getAttribute("deviceGroupIds").toString();
        rolesStr = rolesStr.replaceAll("[\"\\[\\]]", "");
        String url = String.format("%s/count-alarm?whereExp=%s&deviceGroupIds=%s",
                this.endpointUrl, whereExp, rolesStr);
        return this.restTemplate.getForObject(url, Integer.class);
    }

    public Alarm[] getAlarmNameByAlarmType(String alarmType) {
        String url = String.format("%s/get-alarm-name-by-alarm-type?alarmType=%s",
                this.endpointUrl, alarmType);
        return this.restTemplate.getForObject(url,Alarm[].class);
    }

    public Alarm[] getAlarmNameByAlarmId(long alarmId) {
        String url = String.format("%s/get-alarm-name-by-alarm-type-id?alarmId=%s",
                this.endpointUrl, alarmId);
        return this.restTemplate.getForObject(url,Alarm[].class);
    }

    public Alarm[] viewGraphNumberOfAlarmType(String whereExp) {
        String rolesStr = session.getAttribute("deviceGroupIds").toString();
        rolesStr = rolesStr.replaceAll("[\"\\[\\]]", "");
        String url = String.format("%s/view-graph-number-of-alarm-type?whereExp=%s&deviceGroupIds=%s",
                this.endpointUrl, whereExp,rolesStr);
        return this.restTemplate.getForObject(url, Alarm[].class);
    }

    public Alarm[] viewGraphSeverityAlarm(String whereExp) {
        String rolesStr = session.getAttribute("deviceGroupIds").toString();
        rolesStr = rolesStr.replaceAll("[\"\\[\\]]", "");
        String url = String.format("%s/view-graph-severity-alarm?whereExp=%s&deviceGroupIds=%s",
                this.endpointUrl, whereExp, rolesStr);
        return this.restTemplate.getForObject(url, Alarm[].class);
    }
}
