package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.dao.SysDeptLeaderRoleDao;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.*;
import com.qc.printers.common.user.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ISysRoleDeptService iSysRoleDeptService;

    @Autowired
    private ISysRoleMenuService iSysRoleMenuService;

    @Autowired
    private ISysUserRoleService iSysUserRoleService;

    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private SysDeptLeaderRoleDao sysDeptLeaderRoleDao;

    @Autowired
    private ISysMenuService iSysMenuService;

    @Autowired
    private ISysRoleService iSysRoleService;
    @Autowired
    private IUserService iUserService;


    @Override
    public UserInfo getUserInfo(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId为空");
        }
        UserInfo userInfo = new UserInfo();
        User user = userDao.getById(userId);
        if (user == null) {
            log.error("异常的userID{}",userId);

            throw new CustomException("请重试！");
        }

        BeanUtils.copyProperties(user, userInfo);
        // 部门角色为继承制，下级部门继承上级
        SysDept myDept = iSysDeptService.getById(user.getDeptId());
        if (myDept == null) {
            throw new CustomException("部门信息错误，请联系运维解决！");
        }
        //该用户所有的角色
        Set<SysRole> userAllRole = this.getUserAllRole(userId);
        if (iUserService.isSuperAdmin(userAllRole, null)) {
            userInfo.setSysMenus(new HashSet<>(iSysMenuService.list()));
            userInfo.setSysRoles(userAllRole);
            return userInfo;
        }
        //不重复的菜单id
        Set<Long> menuIdList = new HashSet<>();
        for (SysRole roleIdS :
                userAllRole) {
            LambdaQueryWrapper<SysRoleMenu> sysRoleMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysRoleMenuLambdaQueryWrapper.eq(SysRoleMenu::getRoleId, roleIdS.getId());
            List<SysRoleMenu> list1 = iSysRoleMenuService.list(sysRoleMenuLambdaQueryWrapper);
            for (SysRoleMenu sysRoleMenu :
                    list1) {
                menuIdList.add(sysRoleMenu.getMenuId());
            }
        }
        Set<SysMenu> sysMenus = new HashSet<>();
        if(!menuIdList.isEmpty()){
            sysMenus = new HashSet<>(iSysMenuService.listByIds(menuIdList));
        }
        userInfo.setSysMenus(sysMenus);
        userInfo.setSysRoles(userAllRole);
        return userInfo;
    }

    @Override
    public User getUserForCache(Long userId) {

        return  userDao.getById(userId);
    }

    @Cacheable(cacheNames = "roleList", key = "'roleList'+#userId")
    public Set<SysRole> getUserAllRole(Long userId) {
        User user = userDao.getById(userId);
        if (user == null) {
            log.error("异常的userID{}",userId);
            throw new CustomException("请重试！");
        }
        // 部门角色为继承制，下级部门继承上级
        SysDept myDept = iSysDeptService.getById(user.getDeptId());
        if (myDept == null) {
            throw new CustomException("部门信息错误，请联系运维解决！");
        }
        Set<Long> collect;
        if (StringUtils.isNotEmpty(myDept.getAncestors()) && !myDept.getParentId().equals(0L)) {
            collect = Arrays.stream(myDept.getAncestors().split(",")).map(Long::valueOf).collect(Collectors.toSet());
        } else {
            collect = new HashSet<>();
        }
        collect.add(user.getDeptId());
        //该用户所拥有的不重复的roleId
        Set<Long> userRoleIdList = new HashSet<>();
        for (Long itemDeptId : collect) {
            LambdaQueryWrapper<SysRoleDept> roleDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roleDeptLambdaQueryWrapper.eq(SysRoleDept::getDeptId, itemDeptId);
            Set<Long> roleLongIdItem = iSysRoleDeptService.list(roleDeptLambdaQueryWrapper).stream().map(SysRoleDept::getRoleId).collect(Collectors.toSet());
            userRoleIdList.addAll(roleLongIdItem);
        }
        // 如果是部门负责人的话
        if (myDept.getLeaderId()!=null&&myDept.getLeaderId().equals(userId)){
            LambdaQueryWrapper<SysDeptLeaderRole> sysDeptLeaderRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysDeptLeaderRoleLambdaQueryWrapper.eq(SysDeptLeaderRole::getDeptId, myDept.getId());
            Set<Long> collect1 = sysDeptLeaderRoleDao.list(sysDeptLeaderRoleLambdaQueryWrapper).stream().map(SysDeptLeaderRole::getRoleId).collect(Collectors.toSet());

            userRoleIdList.addAll(collect1);
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

        return sysRoles;
    }

}
