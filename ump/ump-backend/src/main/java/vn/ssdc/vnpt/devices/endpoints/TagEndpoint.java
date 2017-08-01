package vn.ssdc.vnpt.devices.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.ssdc.vnpt.devices.model.Tag;
import vn.ssdc.vnpt.devices.services.TagService;
import vn.vnpt.ssdc.core.SsdcCrudEndpoint;

import java.util.List;

/**
 * Created by vietnq on 11/3/16.
 */
@Component
public class TagEndpoint extends SsdcCrudEndpoint<Long,Tag> {

    private   TagService tagService;

    @Autowired
    public TagEndpoint(TagService tagService) {
        this.service = this.tagService = tagService;
    }
}
