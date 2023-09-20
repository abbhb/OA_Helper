package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.*;
import com.qc.printers.common.user.service.*;
import com.qc.printers.common.user.service.cache.UserCache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional
@Service
public class IUserServiceImpl implements IUserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserCache userCache;

    @Autowired
    private ISysRoleDeptService iSysRoleDeptService;

    @Autowired
    private ISysRoleMenuService iSysRoleMenuService;

    @Autowired
    private ISysUserRoleService iSysUserRoleService;

    @Autowired
    private ISysMenuService iSysMenuService;

    @Autowired
    private ISysRoleService iSysRoleService;


    public UserInfo getUserInfo(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId为空");
        }
        UserInfo userInfo = new UserInfo();
        User user = userDao.getById(userId);
        if (user == null) {
            throw new CustomException("请重试！");
        }
        BeanUtils.copyProperties(user, userInfo);
        LambdaQueryWrapper<SysRoleDept> roleDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roleDeptLambdaQueryWrapper.eq(SysRoleDept::getDeptId, user.getDeptId());
        List<SysRoleDept> sysRoleDeptList = iSysRoleDeptService.list(roleDeptLambdaQueryWrapper);
        //该用户所拥有的不重复的roleId
        Set<Long> userRoleIdList = new HashSet<>();
        for (SysRoleDept s :
                sysRoleDeptList) {
            userRoleIdList.add(s.getRoleId());
        }
        LambdaQueryWrapper<SysUserRole> sysUserRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysUserRoleLambdaQueryWrapper.eq(SysUserRole::getUserId, user.getId());
        List<SysUserRole> list = iSysUserRoleService.list(sysUserRoleLambdaQueryWrapper);
        for (SysUserRole sysUserRole :
                list) {
            userRoleIdList.add(sysUserRole.getRoleId());
        }

        Set<SysRole> sysRoles;
        if (userRoleIdList.size() == 0) {
            sysRoles = new HashSet<>();
        } else {
            sysRoles = new HashSet<>(iSysRoleService.listByIds(userRoleIdList));
        }
        if (isSuperAdmin(sysRoles, null)) {
            userInfo.setSysMenus(new HashSet<>(iSysMenuService.list()));
            userInfo.setSysRoles(sysRoles);
            return userInfo;
        }
        //不重复的菜单id
        Set<Long> menuIdList = new HashSet<>();
        for (Long roleId :
                userRoleIdList) {
            LambdaQueryWrapper<SysRoleMenu> sysRoleMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysRoleMenuLambdaQueryWrapper.eq(SysRoleMenu::getRoleId, roleId);
            List<SysRoleMenu> list1 = iSysRoleMenuService.list(sysRoleMenuLambdaQueryWrapper);
            for (SysRoleMenu sysRoleMenu :
                    list1) {
                menuIdList.add(sysRoleMenu.getMenuId());
            }
        }
        Set<SysMenu> sysMenus;
        if (menuIdList.size() == 0) {
            sysMenus = new HashSet<>();

        } else {
            sysMenus = new HashSet<>(iSysMenuService.listByIds(menuIdList));

        }
        if (sysMenus == null) {
            throw new CustomException("请重试！");
        }

        userInfo.setSysMenus(sysMenus);
        userInfo.setSysRoles(sysRoles);
        return userInfo;
    }

    public boolean isSuperAdmin(Set<SysRole> roleSet, Long userId) {
        if (roleSet != null) {
            if (roleSet.stream().anyMatch(sysRole -> sysRole.getRoleKey() != null && sysRole.getRoleKey().equals("userDaoadmin"))) {
                return true;
            }
            return false;
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId为空");
        }
        UserInfo userInfo = new UserInfo();
        User user = userDao.getById(userId);
        if (user == null) {
            throw new CustomException("请重试！");
        }
        BeanUtils.copyProperties(user, userInfo);
        LambdaQueryWrapper<SysRoleDept> roleDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roleDeptLambdaQueryWrapper.eq(SysRoleDept::getDeptId, user.getDeptId());
        List<SysRoleDept> sysRoleDeptList = iSysRoleDeptService.list(roleDeptLambdaQueryWrapper);
        //该用户所拥有的不重复的roleId
        Set<Long> userRoleIdList = new HashSet<>();
        for (SysRoleDept s :
                sysRoleDeptList) {
            userRoleIdList.add(s.getRoleId());
        }
        LambdaQueryWrapper<SysUserRole> sysUserRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysUserRoleLambdaQueryWrapper.eq(SysUserRole::getUserId, user.getId());
        List<SysUserRole> list = iSysUserRoleService.list(sysUserRoleLambdaQueryWrapper);
        for (SysUserRole sysUserRole :
                list) {
            userRoleIdList.add(sysUserRole.getRoleId());
        }
        Set<SysRole> sysRoles = new HashSet<>(iSysRoleService.listByIds(userRoleIdList));
        if (sysRoles.stream().anyMatch(sysRole -> sysRole.getRoleKey() != null && sysRole.getRoleKey().equals("userDaoadmin"))) {
            return true;
        }
        return false;
    }

}
