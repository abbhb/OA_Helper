package com.qc.printers.common.user.domain.dto;

import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * @EqualsAndHashCode
 * @ToString 用来解决子类log.info无法打印父类属性的问题
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class UserInfo extends User {
    //不重复的sysRoles
    private Set<SysRole> sysRoles;

    //不重复的菜单
    private Set<SysMenu> sysMenus;


}
