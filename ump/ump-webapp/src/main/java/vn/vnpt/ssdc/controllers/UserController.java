package vn.vnpt.ssdc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.client.RoleClient;
import vn.vnpt.ssdc.api.client.UserClient;
import vn.vnpt.ssdc.api.model.DeviceGroup;
import vn.vnpt.ssdc.api.model.Role;
import vn.vnpt.ssdc.api.model.User;
import vn.vnpt.ssdc.models.UserPaginator;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class UserController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final String INDEX_URL = "/user";
    private static final String INDEX_PAGE = "user/user";
    private static final String RESPONSE_MESSAGE_PAGE = "user/user_response_message";
    private static final String CHANGE_FORGOT_PASSWORD_PAGE = "user/change_forgot_password";
    private static final String CHANGE_FORGOT_PASSWORD_CONFIRM_PAGE = "user/change_forgot_password_confirm";
    private static final String CHANGE_PASSWORD_PAGE = "user/change_password";
    private static final String FORGOT_PASSWORD_PAGE = "user/forgot_password";

    @Autowired
    UserClient userClient;

    @Autowired
    DeviceGroupClient deviceGroupClient;

    @Autowired
    RoleClient roleClient;

    @Autowired
    RestTemplate restTemplate;

    //<editor-fold desc="Index">
    @GetMapping(INDEX_URL)
    @PreAuthorize("hasAuthority('API:USER:READ-LIST-USERS')")
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "20") int limit,
                        @RequestParam Map<String, String> params
                        ) {
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"go to list users page","","");
        // Get filter search
        StringBuilder whereStr = new StringBuilder();
        String all = params.containsKey("all") ? params.get("all") : "";
        String email = params.containsKey("email") ? params.get("email") : "";
        String userName = params.containsKey("user_name") ? params.get("user_name") : "";
        String fullName = params.containsKey("full_name") ? params.get("full_name") : "";
        String deviceGroupIds = params.containsKey("device_group_ids") ? params.get("device_group_ids") : "";

        // Hide system user
        whereStr.append("user_name <> 'ump' AND ");

        if (!"".equals(all)) {
            if (!"".equals(email)) {
                whereStr.append("email LIKE '%").append(email).append("%'");
            } else {
                whereStr.append("email LIKE '%").append(all).append("%'");
            }

            if (!"".equals(userName)) {
                whereStr.append(" AND user_name LIKE '%").append(userName).append("%'");
            } else {
                whereStr.append(" OR user_name LIKE '%").append(all).append("%'");
            }

            if (!"".equals(fullName)) {
                whereStr.append(" AND full_name LIKE '%").append(fullName).append("%'");
            } else {
                whereStr.append(" OR full_name LIKE '%").append(all).append("%'");
            }

            if (!"".equals(deviceGroupIds)) {
                for (String deviceGroupId : deviceGroupIds.split(",")) {
                    whereStr.append(" AND device_group_ids LIKE '%\"").append(deviceGroupId).append("\"%'");
                }
            } else {
                whereStr.append(" OR device_group_names LIKE '%").append(all).append("%'");
            }
        } else {
            whereStr.append(" email LIKE '%").append(email).append("%'");
            whereStr.append(" AND user_name LIKE '%").append(userName).append("%'");
            whereStr.append(" AND full_name LIKE '%").append(fullName).append("%'");
            if (!"".equals(deviceGroupIds)) {
                for (String deviceGroupId : deviceGroupIds.split(",")) {
                    whereStr.append(" AND device_group_ids LIKE '%\"").append(deviceGroupId).append("\"%'");
                }
            }
        }

        page = page > 1 ? page - 1 : 0;
        // Remove special character
        String alphaAndDigits = whereStr.toString().replaceAll("[#]", "");
        UserPaginator userPaginator = userClient.getPage(page, limit, alphaAndDigits);

        // Check content size to get max page
        if (userPaginator.content.size() == 0 && userPaginator.totalPages >= 1) {
            page = userPaginator.totalPages;
            params.put("page", String.valueOf(page));
            userPaginator = userClient.getPage(page - 1, limit, alphaAndDigits);
        }
        userPaginator.number++;

        // Get data for form modal
        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        String currentUsername = session.getAttribute("username").toString();
        Role[] roles = roleClient.getChildrenRoles(currentUsername);

        model.addAttribute("userPaginator", userPaginator);
        model.addAttribute("deviceGroups", deviceGroupClient.findAll());
        model.addAttribute("roles", roles);
        model.addAttribute("request", params);
        model.addAttribute("currentUsername", session.getAttribute("username"));

        return INDEX_PAGE;
    }
    //</editor-fold>

    //<editor-fold desc="Create & Edit">
    @PreAuthorize("hasAuthority('API:USER:CREATE')")
    @PostMapping(INDEX_URL + "/store")
    public String store(@RequestParam Map<String, String> params,
                        @RequestParam() MultipartFile fileAvatar) {

        User user = new User();
        user.userName = params.get("user_name");
        user.fullName = params.get("full_name");
        user.email = params.get("email");
        user.phone = params.get("phone");
        user.description = params.get("description");

        // Set device group data
        user.deviceGroupIds.addAll(Arrays.asList(params.get("group_device").split(",")));
        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        for (DeviceGroup deviceGroup : deviceGroups) {
            if(user.deviceGroupIds.contains(String.valueOf(deviceGroup.id))) {
                user.deviceGroupNames.add(deviceGroup.name);
            }
        }

        // Set role data
        user.roleIds.addAll(Arrays.asList(params.get("role").split(",")));
        Role[] roles = roleClient.findAll();
        for (Role role : roles) {
            if(user.roleIds.contains(String.valueOf(role.id))) {
                user.roleNames.add(role.name);
            }
        }

        if (fileAvatar != null) {
            // TODO save file
        }
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"create a new user",user.userName,"");
        userClient.create(user);

        return "redirect:" + INDEX_URL;
    }

    @PostMapping(INDEX_URL + "/{id}/update")
    @PreAuthorize("hasAuthority('API:USER:UPDATE')")
    public String update(Model model,
                         @RequestParam Map<String, String> params,
                         @RequestParam() MultipartFile fileAvatar,
                         @PathVariable("id") Long id) {
        User user = userClient.get(id);
        user.userName = params.get("user_name");
        user.fullName = params.get("full_name");
        user.phone = params.get("phone");
        user.description = params.get("description");

        // If email has change
        String message = "";
        if(!user.email.equals(params.get("email"))) {
            message = "Edit user successfully ! The security code sent to their email.";
        }
        user.email = params.get("email");

        // Set device group data
        user.deviceGroupIds = new LinkedHashSet<String>();
        user.deviceGroupNames = new LinkedHashSet<String>();
        user.deviceGroupIds.addAll(Arrays.asList(params.get("group_device").split(",")));
        DeviceGroup[] deviceGroups = deviceGroupClient.findAll();
        for (DeviceGroup deviceGroup : deviceGroups) {
            if(user.deviceGroupIds.contains(String.valueOf(deviceGroup.id))) {
                user.deviceGroupNames.add(deviceGroup.name);
            }
        }

        // Set role data
        user.roleIds = new LinkedHashSet<String>();
        user.roleNames = new LinkedHashSet<String>();
        user.roleIds.addAll(Arrays.asList(params.get("role").split(",")));
        Role[] roles = roleClient.findAll();
        for (Role role : roles) {
            if(user.roleIds.contains(String.valueOf(role.id))) {
                user.roleNames.add(role.name);
            }
        }

        if (fileAvatar != null) {
            // TODO save file
        }

        userClient.update(id, user);

        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"update user",user.userName,"");

        if(!"".equals(message)) {
            model.addAttribute("message", message);
            return RESPONSE_MESSAGE_PAGE;
        }

        return "redirect:" + INDEX_URL;
    }

    @GetMapping(INDEX_URL + "/{id}/get-existed-username")
    @PreAuthorize("hasAuthority('API:USER:READ-LIST-USERS')")
    @ResponseBody
    public Boolean getExistedUsername(@PathVariable(value = "id") Long id,
                              @RequestParam(value = "username", defaultValue = "") String username) {
        Boolean check = false;
        User[] users = userClient.findAll();
        for(User user : users) {
            if(user.userName.toLowerCase().equals(username.toLowerCase()) && !user.id.equals(id)) {
                check = true;
                break;
            }
        }

        return check;
    }

    @GetMapping(INDEX_URL + "/{id}/get-existed-email")
    @PreAuthorize("hasAuthority('API:USER:READ-LIST-USERS')")
    @ResponseBody
    public Boolean getExistedEmail(@PathVariable(value = "id") Long id,
                                   @RequestParam(value = "email", defaultValue = "") String email) {
        Boolean check = false;
        User[] users = userClient.findAll();
        for(User user : users) {
            if(user.email.toLowerCase().equals(email.toLowerCase()) && !user.id.equals(id)) {
                check = true;
                break;
            }
        }

        return check;
    }

    @GetMapping(INDEX_URL + "/{id}/get-edit")
    @PreAuthorize("hasAuthority('API:USER:READ-ONE-USER')")
    @ResponseBody
    public User getEdit(@PathVariable("id") Long id) {
        return userClient.get(id);
    }
    //</editor-fold>

    //<editor-fold desc="Remove">
    @PostMapping(INDEX_URL + "/post-remove")
    @PreAuthorize("hasAuthority('API:USER:DELETE')")
    @ResponseBody
    public Boolean postRemove(@RequestParam Map<String, String> params) {
        for (String key : params.keySet()) {
            if(key.startsWith("ids")){
                Long removeId = Long.valueOf(params.get(key));
                User user = userClient.get(removeId);
                logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"delete user",user.userName,"");
                userClient.delete(removeId);
            }
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Reset password">
    @PostMapping(INDEX_URL + "/{id}/post-reset-password")
    @PreAuthorize("hasAuthority('API:USER:RESET-PASSWORD')")
    @ResponseBody
    public Boolean postResetPassword(@PathVariable("id") Long id) {
        User user = userClient.get(id);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"reset password",user.userName,"");
        userClient.postResetPassword(id);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Change password">
    @GetMapping(INDEX_URL + "/change-password")
    public String changePassword() {
        return CHANGE_PASSWORD_PAGE;
    }

    @PostMapping(INDEX_URL + "/post-check-current-password")
    @ResponseBody
    public Boolean postCheckCurrentPassword(@RequestParam Map<String, String> request) {
        String username = request.containsKey("username") ? request.get("username") : "";
        String currentPassword = request.containsKey("currentPassword") ? request.get("currentPassword") : "";

        if(!username.isEmpty() && !currentPassword.isEmpty()) {
            return userClient.checkCurrentPassword(username, currentPassword);
        } else {
            return false;
        }

    }

    @PostMapping(INDEX_URL + "/change-password")
    public String updateChangePassword(@RequestParam Map<String, String> request) {
        String username = request.containsKey("username") ? request.get("username") : "";
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"change password",username,"");
        String newPassword = request.containsKey("newPassword") ? request.get("newPassword") : "";
        String currentPassword = request.containsKey("currentPassword") ? request.get("currentPassword") : "";

        if(!currentPassword.isEmpty() && !newPassword.isEmpty()) {
            userClient.changePassword(username, currentPassword, newPassword);
        }

        return "redirect:/";
    }
    //</editor-fold>

    //<editor-fold desc="Forgot password">
    @GetMapping(INDEX_URL + "/forgot-password")
    public String forgotPassword(Model model, HttpServletRequest request) {

        String username = String.valueOf(request.getSession().getAttribute("username"));
        User currentUser = userClient.getByUsername(username);
        model.addAttribute("currentUser", currentUser);

        return FORGOT_PASSWORD_PAGE;
    }

    @PostMapping(INDEX_URL + "/forgot-password")
    public String updateForgotPassword(@RequestParam Map<String, String> request) {
        String username = request.containsKey("username") ? request.get("username") : "";
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"forgot password",username,"");
        userClient.forgotPassword(username);
        return "redirect:" + INDEX_URL;
    }

    @PostMapping(INDEX_URL + "/forgot-password-with-email")
    @PreAuthorize("hasAuthority('API:USER:FORGOT-PASSWORD-WITH-EMAIL')")
    @ResponseBody
    public Boolean updateForgotPasswordWithEmail(@RequestParam(value = "email", defaultValue = "") String email) {
        return userClient.forgotPasswordWithEmail(email);
    }


    @GetMapping("/changeForgotPassword")
    public String changeForgotPassword(Model model,
                                       @RequestParam(value = "userId", defaultValue = "0") Long userId,
                                       @RequestParam(value = "token", defaultValue = "") String token) {

        User user = userClient.get(userId);
        if (user != null) {
            model.addAttribute("user", user);
        }

        return CHANGE_FORGOT_PASSWORD_PAGE;
    }

    @GetMapping("/changeForgotPasswordConfirm")
    public String changeForgotPasswordConfirm(Model model,
                                       @RequestParam(value = "userId", defaultValue = "0") Long userId,
                                       @RequestParam(value = "token", defaultValue = "") String token) {

        User user = userClient.get(userId);
        if (user != null) {
            model.addAttribute("user", user);
        }

        return CHANGE_FORGOT_PASSWORD_CONFIRM_PAGE;
    }

    @PostMapping("/change-password-with-token")
    @ResponseBody
    public Boolean changePassword(@RequestParam(value = "newPassword", defaultValue = "") String newPassword,
                                 @RequestParam(value = "userId", defaultValue = "0") Long userId,
                                 @RequestParam(value = "token", defaultValue = "") String token) {
        User user = userClient.get(userId);
        logger.info("#USER_LOG {},{},{},{},{}",session.getId(),session.getAttribute("username"),"change password with token",user.userName,"");
        return userClient.changePasswordWithToken(userId, token, newPassword);
    }
    //</editor-fold>

}
