package com.qc.printers.common.config.security;

import com.qc.printers.common.user.domain.dto.UserDetailsDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 因为activiti7默认使用security,在查询任务报错,要实现此类才能解决,或者当前框架真正使用security
 * 查询数据库获取权限(我这里伪造),已经有自己的权限系统了，为了activiti作此兼容
 *
 * @author qc2003
 * @date 2024/03/04 20:26
 **/
@Component
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询数据库获取权限(我这里伪造),已经有自己的权限系统了，为了activiti作此兼容
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList("admin,ROLE_ACTIVITI_USER");
        UserDetailsDTO user = new UserDetailsDTO();
        user.setUsername(username);
        user.setAuthorities(authorities);
        return user;
    }
}
