package vn.ssdc.vnpt.devices.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.ssdc.vnpt.devices.model.Tag;
import vn.vnpt.ssdc.core.SsdcCrudService;
import vn.vnpt.ssdc.jdbc.factories.RepositoryFactory;

import java.util.List;

/**
 * Created by vietnq on 11/3/16.
 */
@Service
public class TagService extends SsdcCrudService<Long,Tag>{

    @Autowired
    public TagService(RepositoryFactory repositoryFactory) {
        this.repository = repositoryFactory.create(Tag.class);
    }

    public List<Tag> findByDeviceType(Long deviceTypeId) {
        String whereExp = "device_type_id=?";
        return this.repository.search(whereExp,deviceTypeId);
    }
}
