package vn.vnpt.ssdc.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.Parameter;
import vn.vnpt.ssdc.api.model.Tag;
import vn.vnpt.ssdc.models.TagPaginator;

/**
 * Created by vietnq on 11/2/16.
 */
@Component
public class TagClient extends GenericApiClient<Long, Tag> {
    @Autowired
    public TagClient(RestTemplate restTemplate,
                     @Value("${apiEndpointUrl}") String apiEndpointUrl) {
        this.restTemplate = restTemplate;
        this.entityClass = Tag.class;
        this.endpointUrl = apiEndpointUrl + "/tags";
    }

    public Tag[] findTagByRootTag(String rootTagId) {
        String url = String.format("%s/find-by-rootTag?rootTagId=%s",
                this.endpointUrl, rootTagId);
        return this.restTemplate.getForObject(url, Tag[].class);
    }

    public Tag[] findByDeviceTypeVersion(Long deviceTypeVersionId) {
        String url = String.format("%s/find-by-device-type-version?id=%s",
                this.endpointUrl, deviceTypeVersionId);
        return this.restTemplate.getForObject(url, Tag[].class);
    }

    public Tag[] getListRootTag() {
        return this.restTemplate.getForObject(String.format("%s/get-list-root-tag", this.endpointUrl), Tag[].class);
    }

    public Tag[] getProvisioningTagByDeviceTypeVersion(Long deviceTypeVersionId) {
        return this.restTemplate.getForObject(String.format("%s/get-provisioning-tag-by-device-type-version-id?id=%s", this.endpointUrl, deviceTypeVersionId), Tag[].class);
    }

    public Tag[] getListAssigned() {
        return this.restTemplate.getForObject(String.format("%s/get-list-assigned", this.endpointUrl), Tag[].class);
    }

    public Tag[] getListProfiles() {
        return this.restTemplate.getForObject(String.format("%s/get-list-profiles", this.endpointUrl), Tag[].class);
    }

    public Tag[] getListProvisioningTagByRootTagId(Long id) {
        return this.restTemplate.getForObject(String.format("%s/get-list-provisioning-tag-by-root-tag-id?id=%s", this.endpointUrl, id), Tag[].class);
    }

    public Tag[] findAll() {
        return this.restTemplate.getForObject(String.format("%s/", this.endpointUrl), Tag[].class);
    }

    public TagPaginator getPageRootTag(int page, int limit) {
        return this.restTemplate.getForObject(String.format("%s/get-page-root-tag?page=%d&limit=%d",this.endpointUrl, page, limit), TagPaginator.class);
    }

    public Tag[] findSynchronizedByDeviceTypeVersion(Long id) {
        return this.restTemplate.getForObject(String.format("%s/find-synchronized-by-device-type-version?id=%s",this.endpointUrl, id), Tag[].class);
    }

    public Parameter[] getParametersOfDevice(String deviceId, Long tagId) {
        return this.restTemplate.getForObject(String.format("%s/get-parameters-of-device/%s/%s",this.endpointUrl, tagId, deviceId),Parameter[].class);
    }

    public Tag[] getListProfileSynchronized() {
        return this.restTemplate.getForObject(String.format("%s/get-list-profile-synchronized", this.endpointUrl), Tag[].class);
    }

    public Tag getProfileOthers(Long deviceTypeVersionId) {
        return this.restTemplate.getForObject(String.format("%s/get-profile-others?deviceTypeVersionId=%s", this.endpointUrl, deviceTypeVersionId), Tag.class);
    }
}
