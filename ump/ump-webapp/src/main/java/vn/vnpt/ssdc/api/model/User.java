package vn.vnpt.ssdc.api.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class User extends SsdcEntity<Long> {

    public String userName;
    public String fullName;
    public String email;
    public String password;
    public Set<String> roleIds;
    public Set<String> roleNames;
    public Set<String> deviceGroupIds;
    public Set<String> deviceGroupNames;
    public String avatarUrl;
    public String phone;
    public String description;
    public String forgotPwdToken;
    public String forgotPwdTokenRequested;
    public Set<String> operationIds;

    public User() {
        this.roleIds = new LinkedHashSet<String>();
        this.roleNames = new LinkedHashSet<String>();
        this.deviceGroupIds = new LinkedHashSet<String>();
        this.deviceGroupNames = new LinkedHashSet<String>();
        this.operationIds = new LinkedHashSet<String>();
    }
}