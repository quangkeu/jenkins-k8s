package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Alarm;
import vn.vnpt.ssdc.api.model.AlarmDetails;

/**
 * Created by thangnc on 01-Aug-17.
 */
@Component
public class AlarmDetailClient extends GenericApiClient<Long, AlarmDetails>{

    @Autowired
    public AlarmDetailClient(RestTemplate restTemplate,
                       @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = AlarmDetails.class;
        this.endpointUrl = apiEndpointUrl + "/alarm-detail";
    }

    public AlarmDetails[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, AlarmDetails[].class);
    }

    public AlarmDetails[] getAlarmDetailByAlarmId(long alarmId) {
        String url = String.format("%s/get-alarm-detail-by-alarm-type-id?alarmId=%s",
                this.endpointUrl, alarmId);
        return this.restTemplate.getForObject(url,AlarmDetails[].class);
    }

}
