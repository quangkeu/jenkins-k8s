package vn.vnpt.ssdc.controllers;

import com.google.gson.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.vnpt.ssdc.api.client.OperationClient;
import vn.vnpt.ssdc.api.client.PermissionsClient;
import vn.vnpt.ssdc.api.client.RoleClient;
import vn.vnpt.ssdc.api.client.UserClient;
import vn.vnpt.ssdc.api.model.Operation;
import vn.vnpt.ssdc.api.model.Permissions;
import vn.vnpt.ssdc.api.model.Role;
import vn.vnpt.ssdc.api.model.User;
import vn.vnpt.ssdc.models.PermissionPaginator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Lamborgini on 5/8/2017.
 */
@Controller
public class PermissionController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(RoleController.class);
    private static final String ROLE_MANAGEMENT_PAGE = "users/fragments/permission_fragment";

    @Autowired
    private PermissionsClient permissionsClient;

    @Autowired
    private OperationClient operationClient;

    @Autowired
    private RoleClient roleClient;

    @Autowired
    private UserClient userClient;

    @GetMapping("/permission")
    @PreAuthorize("hasAuthority('API:PERMISSION:READ-LIST-PERMISSIONS')")
    public String index(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to list permissions page","","");
        loadData(model, requestParams);
        session.removeAttribute("delete");
        return ROLE_MANAGEMENT_PAGE;
    }

    @GetMapping("/permission/search")
    @PreAuthorize("hasAuthority('API:PERMISSION:READ-LIST-PERMISSIONS')")
    public String search(Model model, @RequestParam Map<String, String> requestParams) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"search on list permissions page","","");
        loadData(model, requestParams);
        session.removeAttribute("delete");
        return ROLE_MANAGEMENT_PAGE;
    }

    @PostMapping("/deletePermission")
    @PreAuthorize("hasAuthority('API:PERMISSION:DELETE')")
    @ResponseBody
    public String deletePermission(@RequestParam Map<String, String> params) {
        String result = "";
        String sessionResult = "";
        if (params.keySet().contains("deleteIds")) {
            String[] paramPermissions = params.get("deleteIds").substring(0, params.get("deleteIds").length() - 1).split(",");
            String[] paramPermissionsName = params.get("deleteNames").substring(0, params.get("deleteNames").length() - 1).split(",");
            if (paramPermissions.length > 0) {
                try {
                    for (int i = 0; i < paramPermissions.length; i++) {
                        Role[] roles = roleClient.checkByPermissionId(paramPermissions[i]);
                        if (roles.length == 0) {
                            permissionsClient.delete(Long.valueOf(paramPermissions[i]));
                            logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete role "+paramPermissions[i],"","");
                        } else {
                            sessionResult += ", " + paramPermissionsName[i];
                        }
                    }
                    result = "success";
                } catch (Exception e) {
                    result = "fail";
                }

                if(sessionResult.startsWith(",")){
                    sessionResult =  sessionResult.substring(2) + " is being used by role !";
                    session.setAttribute("delete",sessionResult);
                }
            }
        }
        return result;
    }

    @PostMapping("/addOrEditPermission")
    @PreAuthorize("hasAuthority('API:PERMISSION:CREATE')")
    @ResponseBody
    public String addOrEditPermission(@RequestParam Map<String, String> params) {
        String result = "";
        if (params.size() > 0) {
            try {

                int resultName = 0;
                if (!params.get("groupNameOldCheck").equals("1")) {
                    resultName = permissionsClient.checkGroupName(params.get("addGroupName"), params.get("addPermission"));
                }
                if (resultName > 0) {
                    result = "Existed permission";
                } else {
                    if (params.get("permissionID") != null) {
                        Permissions permissions = initPermission(params, params.get("permissionID"));
                        permissionsClient.update(Long.valueOf(params.get("permissionID")), permissions);
                        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update permission","","");
                        updateRole(params.get("permissionID"));

                        result = "success";
                    } else {
                        Permissions permissions = initPermission(params, "");
                        permissionsClient.create(permissions);
                        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create permission","","");
                        result = "success";
                    }
                }

            } catch (Exception e) {
                result = "Add/Edit fail. Please try again";
            }

        }
        return result;
    }

    private void updateRole(String permissionId) {

        Role[] roles = roleClient.checkByPermissionId(permissionId);
        if(roles.length > 0){
            List<String> idRoles = new ArrayList<String>();
            for(Role role : roles){
                idRoles.add(String.valueOf(role.getId()));
                Set<Long> setPermission = new HashSet<>();
                Set<String> setOperation = new HashSet<>();

                Set<Long> setPermissionsIds = role.getPermissionsIds();
                for(Long l : setPermissionsIds){
                    setPermission.add(l);
                    Permissions permissions = permissionsClient.get(l);
                    String lol = permissions.getOperationIds().toString().replaceAll(" ","");
                    String[] lolArray = lol.substring(1,lol.length() - 1).trim().split(",");
                    Collections.addAll(setOperation, lolArray);
                }
                role.setOperationIds(setOperation);
                role.setPermissionsIds(setPermission);
                roleClient.update(role.getId(), role);
            }

            for(String id : idRoles){
                User[] users = userClient.checkByRoleId(String.valueOf(id));
                for (User user : users){
                    Set<String> setOperation = new HashSet<>();
                    Set<String> setRoles = user.roleIds;
                    for (String s : setRoles){
                        String lol = roleClient.get(Long.valueOf(s)).getOperationIds().toString().replaceAll("\\s+","");
                        String[] lolArray = lol.substring(1,lol.length() - 1).split(",");
                        Collections.addAll(setOperation, lolArray);
                    }
                    user.operationIds = setOperation;
                    userClient.update(user.getId(),user);
                }
            }


        }

    }

    private Permissions initPermission(Map<String, String> params, String permissionID) {
        Permissions permissions = new Permissions();
        if (!permissionID.equals("")) {
            permissions.setId(Long.valueOf(permissionID));
        }
        permissions.setName(params.get("addPermission"));
        permissions.setGroupName(params.get("addGroupName"));
        permissions.setDescription(params.get("addDescription"));
        String[] addOperations = params.get("addOperations").replace("@", "").split(",");
        Set<String> setAddOperations = new HashSet<>();
        Collections.addAll(setAddOperations, addOperations);
        permissions.setOperationIds(setAddOperations);
        return permissions;
    }

    private void loadData(Model model, Map<String, String> requestParams) {
        PermissionPaginator permissionPaginator = new PermissionPaginator();
        permissionPaginator.operationClient = operationClient;
        permissionPaginator.permissionsClient = permissionsClient;
        permissionPaginator.parseParam(requestParams);
        permissionPaginator.loadResult(requestParams);

        model.addAttribute("paginator", permissionPaginator);
        model.addAttribute("itemPerPage", requestParams.size() == 0 ? "20" : requestParams.get("limit"));
        model.addAttribute("delete",session.getAttribute("delete") != null ? session.getAttribute("delete") : "");

        loadDataOperation(model);

    }

    private void loadDataOperation(Model model) {
        Operation[] operations = operationClient.findAll();
        List<Operation> operationList = new ArrayList<Operation>();
        for (Operation operation : operations) {
            operationList.add(operation);
        }

//        insertOperation(operationList);


        JsonArray jsonArray = new JsonArray();
        for (Operation operation : operationList) {
            List<String> listjsonObj = new ArrayList<String>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject asJsonObject = jsonArray.get(i).getAsJsonObject();
                String asString = asJsonObject.get("title").getAsString();
                listjsonObj.add(asString);
            }
            if (listjsonObj.contains(operation.getGroupName())) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject asJsonObject = jsonArray.get(i).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : asJsonObject.entrySet()) {
                        if (entry.getKey().equals("key") && entry.getValue().toString().replace("\"","").equals(operation.getGroupName())) {
                            JsonArray jsonArray1 = asJsonObject.getAsJsonArray("children");
                            JsonObject jsonObject1 = new JsonObject();
                            jsonObject1.addProperty("title", operation.getName());
                            jsonObject1.addProperty("key", operation.getId());
                            jsonArray1.add(jsonObject1);
                        }
                    }
                }
            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", operation.getGroupName());
                jsonObject.addProperty("key", operation.getGroupName());
                JsonArray jsonArrayChildren = new JsonArray();
                jsonArrayChildren = loadDataOperationChildren(operation);
                jsonObject.add("children", jsonArrayChildren);
                jsonArray.add(jsonObject);
            }

        }
//        System.out.println(">>>>>>>>>>>>>>>>>>>>> : "+jsonArray.toString());
        model.addAttribute("dataTreePermissions", jsonArray.toString());
    }

    private void insertOperation(List<Operation> operationList) {
//        String lol = "{\"$and\":[{\"_tags\":\"Hà Nội\"},{\"_deviceId._OUI\":\"a06518\"},{\"_deviceId._ProductClass\":\"96318REF_P300\"},{\"_tags\":\"Thanh Xuân\"}],\"$or\":[{\"_tags\":\"Hải Dương\"}]}";
//        JsonParser parser = new JsonParser();
//        JsonObject o = parser.parse(lol).getAsJsonObject();
//        JsonArray asJsonArray = o.getAsJsonArray("$and");
//        JsonArray asJsonArray1 = o.getAsJsonArray("$or");

//        try {
//            String dis = "TAGS";
//            Document document = DocumentHelper.createDocument();
//            Element root = document.addElement( "rdbc" );
//            Element supercarElement = root.addElement("group")
//                    .addAttribute("name", dis);
//            for (Operation operation : operationList){
//                if(operation.groupName.equals(dis)){
//                supercarElement.addElement("operation")
//                        .addAttribute("id", operation.id)
//                        .addAttribute("name", operation.name);}
//            }
//
//            // Pretty print the document to System.out
//            OutputFormat format = OutputFormat.createPrettyPrint();
//            XMLWriter writer;
//            writer = new XMLWriter( System.out, format );
//            writer.write( document );
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private JsonArray loadDataOperationChildren(Operation operation) {
        JsonArray jsonArrayChildren = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", operation.getName());
        jsonObject.addProperty("key", operation.getId());
        jsonArrayChildren.add(jsonObject);
        return jsonArrayChildren;
    }
}
