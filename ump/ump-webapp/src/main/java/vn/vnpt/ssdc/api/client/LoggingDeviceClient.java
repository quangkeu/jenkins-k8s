package vn.vnpt.ssdc.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.LoggingDevice;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LoggingDeviceClient extends GenericApiClient<Long, LoggingDevice> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingDeviceClient.class);

    private static final String LOGGING_DEVICE_ENDPOINT = "/logging/device";

    @Autowired
    public LoggingDeviceClient(RestTemplate restTemplate, @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.endpointUrl = apiEndpointUrl + LOGGING_DEVICE_ENDPOINT;
        this.entityClass = LoggingDevice.class;
    }

    public LoggingDevice[] getPage(int page, int limit,
                                   String name, String actor,
                                   String fromDateTime, String toDateTime,
                                   String username) {
        String url = String.format("%s/get-page" +
                        "?page=%d&limit=%d" +
                        "&name=%s&actor=%s" +
                        "&fromDateTime=%s&toDateTime=%s" +
                        "&username=%s",
                endpointUrl, page, limit, name, actor, fromDateTime, toDateTime, username);
        return this.restTemplate.getForObject(url, LoggingDevice[].class);
    }

    public Long getTotalPages(int page, int limit,
                              String name, String actor,
                              String fromDateTime, String toDateTime,
                              String username) {
        String url = String.format("%s/get-total-pages" +
                        "?page=%d&limit=%d" +
                        "&name=%s&actor=%s" +
                        "&fromDateTime=%s&toDateTime=%s" +
                        "&username=%s",
                endpointUrl, page, limit, name, actor, fromDateTime, toDateTime, username);
        return this.restTemplate.getForObject(url, Long.class);
    }

    public void exportXML(String session, HttpServletResponse response) {
        try {
            URL url = new URL(this.endpointUrl + "/export/" + session);
            response.setHeader("Content-disposition", "attachment;filename=session-" + session + ".xml");
            response.setContentType("application/xml");

            InputStream is = url.openStream();

            BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
            int len;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) > 0) {
                outs.write(buf, 0, len);
            }
            outs.close();

        } catch (MalformedURLException e) {
            logger.error("Error ModelAndView.viewMain - MalformedURLException : " + e.toString() + " -- " + e.getStackTrace()[0].toString());
        } catch (IOException e) {
            logger.error("Error ModelAndView.viewMain - IOException : " + e.toString() + " -- " + e.getStackTrace()[0].toString());
        }
    }

    public Boolean removeAll(String name, String actor, String fromDateTime, String toDateTime) {
        String url = String.format("%s/remove-all?name=%s&actor=%s&fromDateTime=%s&toDateTime=%s", endpointUrl, name, actor, fromDateTime, toDateTime);
        return this.restTemplate.postForObject(url, null, Boolean.class);
    }

    public Boolean saveTimeExpire(String timeExpire) {
        String url = String.format("%s/post-save-time-expire", endpointUrl);
        Map<String, String> params = new HashMap<String, String>();
        params.put("timeExpire", timeExpire);
        return this.restTemplate.postForObject(url, params, Boolean.class);
    }
}
