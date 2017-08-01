package vn.ssdc.vnpt.devices.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import vn.ssdc.vnpt.AcsClient;
import vn.ssdc.vnpt.dto.AcsResponse;
import vn.vnpt.ssdc.utils.ObjectUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * REST API for Device Management <br/>
 *
 * Created by vietnq on 10/31/16.
 */
@Component
@Path(("/api/acs"))
@Produces(APPLICATION_JSON)
public class AcsEndpoint {
    @Autowired
    private AcsClient acsClient;

    @GET
    public AcsResponse searchDevices(@DefaultValue("{}") @QueryParam("query") String query,
                                     @DefaultValue("50") @QueryParam("limit") String limit,
                                     @DefaultValue("0") @QueryParam("offset") String offset,
                                     @QueryParam("parameters") String parameters,
                                     @QueryParam("sort") String sort) {
        AcsResponse response = new AcsResponse();
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("query", query);
        queryParams.put("limit", limit);
        queryParams.put("skip", offset);
        if (!ObjectUtils.empty(parameters)) {
            queryParams.put("projection", parameters);
        }
        if (!ObjectUtils.empty(sort)) {
            queryParams.put("sort", sort);
        }
        ResponseEntity<String> responseEntity = this.acsClient.search("devices", queryParams);
        response.httpResponseCode = responseEntity.getStatusCodeValue();
        //total of search result is in http header [total]
        response.nbOfItems = Integer.parseInt(responseEntity.getHeaders().get("total").get(0));
        response.body = responseEntity.getBody();
        return response;
    }


    /**
     * Reboot a specific device.
     *
     * @param deviceId The ID of the device
     * @param timeout
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    @POST
    @Path(("/{deviceId}/reboot"))
    public AcsResponse rebootDeviceById(@PathParam("deviceId") String deviceId,
                                        @DefaultValue("3000") @QueryParam("timeout") String timeout,
                                        @DefaultValue("false") @QueryParam("now") boolean now) {
        AcsResponse response = new AcsResponse();
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("timeout", timeout);
        if (now == true) {
            queryParams.put("connection_request", "connection_request");
        }
        try {
            ResponseEntity<String> responseEntity = acsClient.reboot(deviceId, queryParams);
            response.httpResponseCode = responseEntity.getStatusCode().value();
        } catch (RestClientException e) {
            response.httpResponseCode = HttpStatus.NOT_FOUND.value();

        }
        return response;
    }

    /**
     * Factory reset a specific device.
     *
     * @param deviceId
     * @param timeout
     * @return 202 if the tasks have been queued to be executed at the next inform.
     * 404 Not found
     * status code 200 if tasks have been successfully executed
     */
    @POST
    @Path(("/{deviceId}/factoryReset"))
    public AcsResponse factoryResetDeviceById(@PathParam("deviceId") String deviceId,
                                              @DefaultValue("3000") @QueryParam("timeout") String timeout,
                                              @DefaultValue("false") @QueryParam("now") boolean now) {
        AcsResponse response = new AcsResponse();
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("timeout", timeout);
        if (now == true) {
            queryParams.put("connection_request", "connection_request");
        }
        try {
            ResponseEntity<String> responseEntity = acsClient.factoryReset(deviceId, queryParams);
            response.httpResponseCode = responseEntity.getStatusCode().value();
        } catch (RestClientException e) {
            response.httpResponseCode = HttpStatus.NOT_FOUND.value();
        }
        return response;
    }


}
