package vn.vnpt.ssdc.api.model;

import java.util.Set;

/**
 * Created by Lamborgini on 5/5/2017.
 */
public class Permissions extends SsdcEntity<Long>{

    public String name;
    public String groupName;
    public String description;
    public Set<String> operationIds;

    public Permissions() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
