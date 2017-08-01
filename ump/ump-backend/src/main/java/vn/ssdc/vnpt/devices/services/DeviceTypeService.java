package vn.ssdc.vnpt.devices.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import vn.ssdc.vnpt.devices.model.DeviceType;
import vn.vnpt.ssdc.core.SsdcCrudService;
import vn.vnpt.ssdc.jdbc.factories.RepositoryFactory;

/**
 * Created by vietnq on 11/1/16.
 */
@Service
public class DeviceTypeService extends SsdcCrudService<Long,DeviceType> {
    @Autowired
    public DeviceTypeService(RepositoryFactory repositoryFactory) {
        this.repository = repositoryFactory.create(DeviceType.class);
    }
}
