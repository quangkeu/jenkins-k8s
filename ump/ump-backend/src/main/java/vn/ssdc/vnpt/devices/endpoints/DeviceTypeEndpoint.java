package vn.ssdc.vnpt.devices.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import vn.ssdc.vnpt.devices.model.DeviceType;
import vn.ssdc.vnpt.devices.model.Tag;
import vn.ssdc.vnpt.devices.services.DeviceTypeService;
import vn.ssdc.vnpt.devices.services.TagService;
import vn.vnpt.ssdc.core.SsdcCrudEndpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
/**
 * Created by vietnq on 11/1/16.
 */
@Component
@Path("/api/device-types")
@Produces(APPLICATION_JSON)
public class DeviceTypeEndpoint extends SsdcCrudEndpoint<Long,DeviceType> {
    private TagService tagService;
    @Autowired
    public DeviceTypeEndpoint(DeviceTypeService deviceVersionService,
                              TagService tagService) {
        this.service = deviceVersionService;
        this.tagService = tagService;
    }

    @GET
    @Path("/{id}/tags")
    public List<Tag> findTagsById(@PathParam("id") Long id) {
        return tagService.findByDeviceType(id);
    }

    @POST
    @Path("/{id}/tags")
    public List<Tag> assignTags(@PathParam("id") Long id, List<Long> tagsID) {
        List<Tag> tags = new ArrayList<Tag>();
        for(Long tagID : tagsID) {
            Tag tag = tagService.get(tagID);
            tag.assigned = 1;
            tagService.update(tagID,tag);
            tags.add(tag);
        }
        return tags;
    }
}
