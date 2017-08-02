package vn.vnpt.ssdc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.client.UserClient;
import vn.vnpt.ssdc.api.model.DeviceGroup;
import vn.vnpt.ssdc.api.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vietnq on 12/15/16.
 */
@Component
public class UmpAuthSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private HttpSession session;

    @Autowired
    private DeviceGroupClient deviceGroupClient;

    @Autowired
    private UserClient userClient;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = new HashSet<String>();
        for(GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        session.setAttribute("roles",roles);
        session.setAttribute("username",authentication.getName());

        User user = userClient.getByUsername(authentication.getName());
        Set<String> queries = new HashSet<>();
        for(String deviceGroupId : user.deviceGroupIds){
            DeviceGroup deviceGroup = deviceGroupClient.get(Long.parseLong(deviceGroupId));
            queries.add(deviceGroup.query);
        }
        String query = "{\"_id\":\"\"}";
        if(queries.size()>0) {
            query = "{\"$or\":[" + StringUtils.join(queries, ",") + "]}";
        }
        session.setAttribute("query",query);
        session.setAttribute("deviceGroupIds",user.deviceGroupIds);
        httpServletResponse.sendRedirect("/");
    }
}
