package vn.vnpt.ssdc.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.vnpt.ssdc.api.client.OperationClient;
import vn.vnpt.ssdc.api.client.PermissionsClient;
import vn.vnpt.ssdc.api.client.RoleClient;
import vn.vnpt.ssdc.api.client.UserClient;
import vn.vnpt.ssdc.api.model.*;
import vn.vnpt.ssdc.models.RolePaginator;
import vn.vnpt.ssdc.utils.ObjectUtils;

import java.util.*;

/**
 * Created by Lamborgini on 5/4/2017.
 */
@Controller
public class RoleController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(RoleController.class);
    private static final String ROLE_MANAGEMENT_PAGE = "users/fragments/role_fragment";

    @Autowired
    private RoleClient roleClient;

    @Autowired
    private PermissionsClient permissionsClient;

    @Autowired
    private OperationClient operationClient;

    @Autowired
    private UserClient userClient;

    @GetMapping("/role")
    @PreAuthorize("hasAuthority('API:ROLE:READ-LIST-ROLES')")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        loadData(model, requestParams);
        session.removeAttribute("delete");
        return ROLE_MANAGEMENT_PAGE;
    }

    @GetMapping("/role/search")
    @PreAuthorize("hasAuthority('API:ROLE:READ-LIST-ROLES')")
    public String search(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"search on list roles page","","");
        loadData(model, requestParams);
        session.removeAttribute("delete");
        return ROLE_MANAGEMENT_PAGE;
    }

    @PostMapping("/deleteRole")
    @PreAuthorize("hasAuthority('API:ROLE:DELETE')")
    @ResponseBody
    public String deleteRole(@RequestParam Map<String, String> params) {
        String result = "";
        String sessionResult = "";
        if (params.keySet().contains("deleteIds")) {
            String[] paramRole = params.get("deleteIds").substring(0, params.get("deleteIds").length() - 1).split(",");
            String[] paramNames = params.get("deleteNames").substring(0, params.get("deleteNames").length() - 1).split(",");
            if (paramRole.length > 0) {
                try {
                    for (int i = 0; i < paramRole.length; i++) {
                        User[] users = userClient.checkByRoleId(paramRole[i]);
                        if (users.length == 0) {
                            roleClient.delete(Long.valueOf(paramRole[i]));
                            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete role "+paramRole[i],"","");
                        } else {
                            sessionResult += ", " + paramNames[i];
                        }
                    }
                    result = "success";
                } catch (Exception e) {
                    result = "fail";
                }

                if(sessionResult.startsWith(",")){
                    sessionResult =  sessionResult.substring(2) + " is being used by user !";
                    session.setAttribute("delete",sessionResult);
                }
            }
        }
        return result;
    }

    @PostMapping("/addOrEditRole")
    @PreAuthorize("hasAuthority('API:ROLE:CREATE')")
    @ResponseBody
    public String addOrEditRole(@RequestParam Map<String, String> params) {
        String result = "";
        if (params.size() > 0) {
            try {

                int resultName = 0;
                if (!params.get("roleNameOldCheck").equals("1")) {
                    resultName = roleClient.checkRoleName(params.get("addNameRole"));
                }
                if (resultName > 0) {
                    result = "Existed role";
                } else {
                    if (params.get("roleID") != null) {
                        Role role = initRole(params, params.get("roleID"));
                        roleClient.update(Long.valueOf(params.get("roleID")), role);
                        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update role","","");
                        updateRoleName(params.get("roleID"));

                        result = "success";
                    } else {
                        Role role = initRole(params, "");
                        Role role1 = roleClient.create(role);
                        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create new role","","");
                        Set<Long> setPermission = role1.getPermissionsIds();
                        Set<String> setOperation = new HashSet<>();
                        for(Long l : setPermission){
                            Permissions permissions = permissionsClient.get(l);
                            String operationIds = permissions.getOperationIds().toString().replaceAll("\\s+","");
                            String[] operationIdsArray = operationIds.substring(1,operationIds.length() - 1).trim().split(",");
                            Collections.addAll(setOperation, operationIdsArray);
                        }
                        role1.setOperationIds(setOperation);
                        roleClient.update(role1.getId(), role1);

                        result = "success";
                    }
                }

            } catch (Exception e) {
                result = "Add/Edit fail. Please try again";
            }

        }
        return result;
    }

    private void updateRoleName(String roleID) {

        Role role = roleClient.get(Long.valueOf(roleID));
        Set<Long> setPermission = role.getPermissionsIds();
        Set<String> setOperation = new HashSet<>();
        for(Long l : setPermission){
            Permissions permissions = permissionsClient.get(l);
            String operationIds = permissions.getOperationIds().toString().replaceAll("\\s+","");
            String[] operationIdsArray = operationIds.substring(1,operationIds.length() - 1).trim().split(",");
            Collections.addAll(setOperation, operationIdsArray);
        }
        role.setOperationIds(setOperation);
        roleClient.update(role.getId(), role);

        User[] users = userClient.checkByRoleId(String.valueOf(roleID));
        for (User user : users){
            Set<String> setOperation1 = new HashSet<>();
            Set<String> setRoles = user.roleIds;
            for (String s : setRoles){
                String lol = roleClient.get(Long.valueOf(s)).getName();
                setOperation1.add(lol);
            }
            user.roleNames = setOperation1;
            userClient.update(user.getId(),user);
        }

    }

    private Role initRole(Map<String, String> params, String roleID) {
        Role role = new Role();
        if (!roleID.equals("")) {
            role.setId(Long.valueOf(roleID));
        }
        role.setName(params.get("addNameRole"));
        role.setDescription(params.get("addDescriptionRole"));
        String[] addPermissions = params.get("addPermission").replace("*", "").split("@");

        Long[] addPermissionsLong = new Long[addPermissions.length];
        String[] addOperations = new String[addPermissions.length];

        try {
            for (int i = 0; i < addPermissions.length; i++) {
                String[] split = addPermissions[i].split("-");
                addPermissionsLong[i] = Long.valueOf(split[0]);
                addOperations[i] = split[1].replace("[", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Set<Long> setAddPermissions = new HashSet<>();
        Collections.addAll(setAddPermissions, addPermissionsLong);
        role.setPermissionsIds(setAddPermissions);
        Set<String> setAddOperations = new HashSet<>();
        Collections.addAll(setAddOperations, addOperations);
        role.setOperationIds(setAddOperations);
        return role;
    }


    private void loadData(Model model, Map<String, String> requestParams) {
        RolePaginator rolePaginator = new RolePaginator();
        rolePaginator.roleClient = roleClient;
        rolePaginator.permissionsClient = permissionsClient;
        rolePaginator.parseParam(requestParams);
        rolePaginator.loadResult(requestParams);

        model.addAttribute("paginator", rolePaginator);
        model.addAttribute("itemPerPage", requestParams.size() == 0 ? "20" : requestParams.get("limit"));
        model.addAttribute("delete",session.getAttribute("delete") != null ? session.getAttribute("delete") : "");

        loadDataPermission(model);
    }

    private void loadDataPermission(Model model) {
        Permissions[] permissions = permissionsClient.findAll();
        List<Permissions> permissionList = new ArrayList<Permissions>();
        for (Permissions permission : permissions) {
            permissionList.add(permission);
        }
        JsonArray jsonArray = new JsonArray();
        for (Permissions permission : permissionList) {
            List<String> listjsonObj = new ArrayList<String>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject asJsonObject = jsonArray.get(i).getAsJsonObject();
                String asString = asJsonObject.get("title").getAsString();
                listjsonObj.add(asString);
            }
            if (listjsonObj.contains(permission.getGroupName())) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject asJsonObject = jsonArray.get(i).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : asJsonObject.entrySet()) {
                        if (entry.getKey().equals("key") && entry.getValue().toString().replace("\"", "").equals(permission.getGroupName())) {
                            JsonArray jsonArray1 = asJsonObject.getAsJsonArray("children");
                            JsonObject jsonObject1 = new JsonObject();
                            jsonObject1.addProperty("title", permission.getName());
                            jsonObject1.addProperty("key", permission.getId() + "-" + permission.getOperationIds());
                            jsonArray1.add(jsonObject1);
                        }
                    }
                }
            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", permission.getGroupName());
                jsonObject.addProperty("key", permission.getGroupName());
                JsonArray jsonArrayChildren = new JsonArray();
                jsonArrayChildren = loadDataPermissionChildren(permission);
                jsonObject.add("children", jsonArrayChildren);
                jsonArray.add(jsonObject);
            }

        }

        model.addAttribute("dataTreePermissions", jsonArray.toString());
    }

    private JsonArray loadDataPermissionChildren(Permissions permission) {
        JsonArray jsonArrayChildren = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", permission.getName());
        jsonObject.addProperty("key", permission.getId() + "-" + permission.getOperationIds());
        jsonArrayChildren.add(jsonObject);
        return jsonArrayChildren;
    }
}