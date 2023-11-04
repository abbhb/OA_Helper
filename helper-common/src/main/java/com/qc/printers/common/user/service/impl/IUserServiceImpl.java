package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.SysRoleDept;
import com.qc.printers.common.user.domain.entity.SysUserRole;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.*;
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
    private ISysRoleDeptService iSysRoleDeptService;

    @Autowired
    private ISysRoleMenuService iSysRoleMenuService;

    @Autowired
    private ISysUserRoleService iSysUserRoleService;

    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private ISysMenuService iSysMenuService;

    @Autowired
    private ISysRoleService iSysRoleService;

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
