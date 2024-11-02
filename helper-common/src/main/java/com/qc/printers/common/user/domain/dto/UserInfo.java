package com.qc.printers.common.user.domain.dto;

import com.qc.printers.common.websocket.domain.entity.IpInfo;
import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @EqualsAndHashCode
 * @ToString 用来解决子类log.info无法打印父类属性的问题
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

@Data
public class UserInfo extends User implements Serializable {
    public UserInfo(LocalDateTime createTime, LocalDateTime updateTime, Integer isDeleted, Long id, String username, String name, String phone, String sex, String studentId, Integer status, Long deptId, String email, String avatar, String password, String salt, Long createUser, LocalDateTime loginDate, IpInfo loginIp, Integer activeStatus, Set<SysRole> sysRoles, Set<SysMenu> sysMenus) {
        super(createTime, updateTime, isDeleted, id, username, name, phone, sex, studentId, status, deptId, email, avatar, password, salt, createUser, loginDate, loginIp, activeStatus);
        this.sysRoles = sysRoles;
        this.sysMenus = sysMenus;
    }

    public UserInfo(Set<SysRole> sysRoles, Set<SysMenu> sysMenus) {
        this.sysRoles = sysRoles;
        this.sysMenus = sysMenus;
    }

    public UserInfo(User user) {
        BeanUtils.copyProperties(user, this);
    }

    public UserInfo() {
    }

    //不重复的sysRoles
    private Set<SysRole> sysRoles;

    //不重复的菜单
    private Set<SysMenu> sysMenus;

}
