package vn.vnpt.ssdc.models;

import vn.vnpt.ssdc.api.client.PermissionsClient;
import vn.vnpt.ssdc.api.client.RoleClient;
import vn.vnpt.ssdc.api.model.Permissions;
import vn.vnpt.ssdc.api.model.Role;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Lamborgini on 5/5/2017.
 */
public class RolePaginator extends Paginator<Role> {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String PAGE_SIZE = "limit";
    public static final String PAGE_QUERY = "query";
    public static final String PAGE_OFFSET = "offset";
    public static final String PAGE_PARAM = "parameters";
    public static final String PAGE_CURRENT = "indexPage";
    public Map<String, String> rolesQueryParam = new HashMap<>();
    public Map<String, String> indexParams = new HashMap<>();
    public int lastPage;
    public int currentPage;
    public int pageSize = DEFAULT_PAGE_SIZE;
    public RoleClient roleClient;
    public PermissionsClient permissionsClient;
    public List<Role> roleList = new ArrayList<Role>();

    public RolePaginator() {
        this.currentPage = 1;
    }

    public void parseParam(Map<String, String> requestParams) {
        String queryInput = null;
        this.number++;
        if (requestParams != null && !requestParams.isEmpty()) {

            if (requestParams.containsKey(PAGE_CURRENT) && requestParams.get(PAGE_CURRENT) != null) {
                this.currentPage = Integer.parseInt(requestParams.get(PAGE_CURRENT));
                this.number = Integer.parseInt(requestParams.get(PAGE_CURRENT));
            }
            if (requestParams.containsKey(PAGE_SIZE) && requestParams.get(PAGE_SIZE) != null) {
                if (!requestParams.get(PAGE_SIZE).toString().equals("0"))
                    this.pageSize = Integer.parseInt(requestParams.get(PAGE_SIZE));
            }
            if (requestParams.containsKey(PAGE_QUERY) && requestParams.get(PAGE_QUERY) != null) {
                queryInput = requestParams.get(PAGE_QUERY).toString();

            }

        }
        this.rolesQueryParam.put(PAGE_PARAM, String.join(",", this.indexParams.keySet()));
        this.rolesQueryParam.put(PAGE_SIZE, String.valueOf(this.pageSize));
        if (queryInput != null) {
            this.rolesQueryParam.put(PAGE_QUERY, queryInput);
        }
        int offset = (this.currentPage - 1) * this.pageSize;
        this.rolesQueryParam.put(PAGE_OFFSET, String.valueOf(offset));
        this.rolesQueryParam.put(PAGE_CURRENT, currentPage != 0 ? String.valueOf(currentPage - 1) : "0");

    }

    public void loadResult(Map<String, String> requestParams) {

        Role[] roles = roleClient.findRole(rolesQueryParam);
        for (Role role : roles) {
            roleList.add(role);
        }
        this.totalPages = roleClient.findAll().length;

        if (this.totalPages == 0) {
            this.lastPage = 1;
        } else {
            int page = this.totalPages % (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            int pageTotal = this.totalPages / (requestParams.size() == 0 ? DEFAULT_PAGE_SIZE
                    : Integer.valueOf(requestParams.get(PAGE_SIZE)));
            this.lastPage = page == 0 ? pageTotal : pageTotal + 1;
        }


    }

    public void loadResultPermission() {
        Permissions[] permissionsList = permissionsClient.findAll();
        System.out.printf("LOL");
    }
}