package vn.vnpt.ssdc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vnpt.ssdc.api.client.DeviceGroupClient;
import vn.vnpt.ssdc.api.client.UserClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by THANHLX on 6/6/2017.
 */
@Service("userDetailsService")
public class UmpUserDetailsService implements UserDetailsService {

    @Autowired
    private UserClient userClient;

    @Transactional(readOnly=true)
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        vn.vnpt.ssdc.api.model.User user = userClient.getByUsername(username);
        Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();
        for(String roleName : user.roleNames){
            setAuths.add(new SimpleGrantedAuthority(roleName));
        }
        for(String operationId : user.operationIds){
            setAuths.add(new SimpleGrantedAuthority(operationId));
        }
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(setAuths);
        return new User(user.userName, user.password, authorities);
    }
}
