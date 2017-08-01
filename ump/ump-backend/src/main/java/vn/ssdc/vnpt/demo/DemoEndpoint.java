package vn.ssdc.vnpt.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.vnpt.ssdc.core.SsdcCrudEndpoint;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
/**
 * Created by vietnq on 10/31/16.
 */
@Component
@Path("/demos")
@Produces(APPLICATION_JSON)
public class DemoEndpoint extends SsdcCrudEndpoint<Long,Demo> {
    @Autowired
    public DemoEndpoint(DemoService demoService) {
        this.service = demoService;
    }
}
