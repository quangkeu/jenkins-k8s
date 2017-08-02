package vn.vnpt.ssdc.api.model;

/**
 * Created by Lamborgini on 5/9/2017.
 */
public class Operation extends SsdcEntity<String> {

    public String name;
    public String groupName;
    public String description;

    public Operation(String name, String groupName, String description) {
        this.name = name;
        this.groupName = groupName;
        this.description = description;
    }

    public Operation() {
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
}
