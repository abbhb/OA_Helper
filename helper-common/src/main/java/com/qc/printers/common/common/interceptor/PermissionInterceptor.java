package com.qc.printers.common.common.interceptor;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.common.user.domain.entity.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 在拦截器中，如果请求为OPTIONS请求，则返回true，表示可以正常访问，然后就会收到真正的GET/POST请求
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            System.out.println("OPTIONS请求，放行");
            return true;
        }
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.getAnnotation(NeedToken.class) == null) {
            //如果没有前置条件 需要登陆
            //权限校验直接通过
            return true;
        }
        if (method.getAnnotation(PermissionCheck.class) == null) {
            return true;
        }
        //权限校验 拥有角色或者小权限其一即可放行
        //需要的权限
        String[] needRole = method.getAnnotation(PermissionCheck.class).role();
        String needPermission = method.getAnnotation(PermissionCheck.class).permission();
        if (needRole.length == 0 && StringUtils.isEmpty(needPermission)) {
            return true;
        }
        List<String> needRoleList = Arrays.asList(needRole);
        Set<String> needRoleSet = new HashSet<>(needRoleList);
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("没有权限");
        }
        Set<SysMenu> sysMenus = currentUser.getSysMenus();
        // 高效查找 needPermission 是否存在于 sysMenus 中的某个对象的某个值中
        boolean exists = sysMenus.stream()
                .filter(sysMenu -> StringUtils.isNotEmpty(sysMenu.getPerms()))
                .map(SysMenu::getPerms)
                .anyMatch(s -> s.equals(needPermission));
        if (exists) {
            return true;
        }
        Set<SysRole> sysRoles = currentUser.getSysRoles();


        // 获取两个集合的交集
        Set<String> intersection = sysRoles.stream()
                .map(SysRole::getRoleKey)
                .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        intersection.retainAll(needRoleSet);
        // 判断交集是否存在,存在即有权限
        boolean hasIntersection = intersection.size() > 0;
        if (hasIntersection) {
            return true;
        }
        throw new CustomException("权限不足");

    }
}
