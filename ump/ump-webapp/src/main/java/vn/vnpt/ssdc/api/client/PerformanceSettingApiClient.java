package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.PerformanceSetting;
import vn.vnpt.ssdc.api.model.PerformanceStatisticsELK;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by thangnc on 21-Jun-17.
 */
@Component
public class PerformanceSettingApiClient extends GenericApiClient<Long, PerformanceSetting> {

    @Autowired
    public PerformanceSettingApiClient(RestTemplate restTemplate, @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = PerformanceSetting.class;
        this.endpointUrl = apiEndpointUrl + "/performance";
    }

    public PerformanceSetting[] findAll() {
        return this.restTemplate.getForObject(endpointUrl, PerformanceSetting[].class);
    }

    public PerformanceSetting[] getPerformanceByPage(int offset, int limit) {
        String url = String.format("%s/getPerformanceByPage", this.endpointUrl);
        url += "?offset=" + offset + "&limit=" + limit;
        return this.restTemplate.getForObject(url, PerformanceSetting[].class);
    }

    public PerformanceSetting[] search(String traffic, String monitoring, String startDate, String endDate, String prefix) {
        String url = String.format("%s/search", this.endpointUrl);
        url += "?traffic=" + traffic + "&monitoring=" + monitoring + "&startDate=" + startDate + "&endDate=" + endDate + "&prefix=" + prefix;
        return this.restTemplate.getForObject(url, PerformanceSetting[].class);
    }

    public PerformanceSetting[] findPerformanceTypeByGroupID(String groupId) {
        return this.restTemplate.getForObject(String.format("%s/get-performance-by-group?groupId=%s",
                this.endpointUrl, groupId), PerformanceSetting[].class);
    }

    public PerformanceStatisticsELK[] searchPerformanceStatistics(String deviceId, String performanceSettingId, String startDate, String endDate) {
        String url = String.format("%s/searchPerformanceStatitics", this.endpointUrl);
        url += "?deviceId=" + deviceId + "&performanceSettingId=" + performanceSettingId + "&startDate=" + startDate + "&endDate=" + endDate;
        return this.restTemplate.getForObject(url, PerformanceStatisticsELK[].class);
    }

    public boolean deleteStatiticsInterface(String deviceId, String performanceSettingId, String stasticsInterface) {
        String url = String.format("%s/deleteStatiticsInterface", this.endpointUrl);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("deviceId", deviceId);
        data.put("performanceSettingId", performanceSettingId);
        data.put("stasticsInterface", stasticsInterface);

        return this.restTemplate.postForObject(url, data, Boolean.class);
    }

    public void exportExcel(Map<String, String> params, HttpServletResponse response) {
        try {
            String paramsExport = params.get("paramExport").replaceAll(" ","T");
            URL url = new URL(this.endpointUrl + "/exportExcel/"+paramsExport);
            response.setHeader("Content-disposition", "attachment;filename=Traffic_Statistic.xlsx");
            response.setContentType("application/vnd.ms-excel");
            InputStream is = url.openStream();
            BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
            int len;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) > 0) {
                outs.write(buf, 0, len);
            }
            outs.close();

        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }
    }
}
