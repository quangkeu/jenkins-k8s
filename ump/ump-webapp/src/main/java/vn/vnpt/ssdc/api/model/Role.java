package vn.vnpt.ssdc.api.model;

import java.util.Set;

/**
 * Created by Lamborgini on 5/4/2017.
 */
public class Role extends SsdcEntity<Long>{
    public String name;
    public Set<Long> permissionsIds;
    public String description;
    public Set<String> operationIds;

    public Role() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getPermissionsIds() {
        return permissionsIds;
    }

    public void setPermissionsIds(Set<Long> permissionsIds) {
        this.permissionsIds = permissionsIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(Set<String> operationIds) {
        this.operationIds = operationIds;
    }
}
