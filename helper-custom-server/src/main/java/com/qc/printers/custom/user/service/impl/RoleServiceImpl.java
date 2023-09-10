package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.domain.dto.RoleMenuDTO;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.SysRoleMenu;
import com.qc.printers.common.user.mapper.SysRoleMapper;
import com.qc.printers.common.user.service.ISysRoleMenuService;
import com.qc.printers.common.user.service.ISysRoleService;
import com.qc.printers.custom.user.domain.vo.response.role.RoleManger;
import com.qc.printers.custom.user.domain.vo.response.role.RoleMangerRoot;
import com.qc.printers.custom.user.service.MenuService;
import com.qc.printers.custom.user.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private ISysRoleService iSysRoleService;

    @Autowired
    private ISysRoleMenuService iSysRoleMenuService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private MenuService menuService;

    @Override
    public List<String> getroleNameByKey(String key) {
        Set<String> needKey = new HashSet<>();
        List<String> allKeyName = new ArrayList<>();
        if (key.contains(",")) {
            needKey = Arrays.stream(key.split(",")).filter(String::isEmpty).collect(Collectors.toSet());
        } else {
            needKey.add(key);
        }
        for (String need :
                needKey) {
            LambdaQueryWrapper<SysRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SysRole::getRoleKey, need);
            SysRole one = iSysRoleService.getOne(lambdaQueryWrapper);
            if (one == null) {
                throw new RuntimeException("角色异常");
            }
            allKeyName.add(one.getRoleName());
        }
        return allKeyName;
    }

    @Override
    public RoleMangerRoot getRoleList() {
        List<RoleMenuDTO> allSysRoleMenu = sysRoleMapper.getAllSysRoleMenu();
        RoleMangerRoot roleMangerRoot = new RoleMangerRoot();

        List<RoleManger> mangers = new ArrayList<>();
        for (RoleMenuDTO sysRoleMenu : allSysRoleMenu) {
            List<SysRoleMenu> sysRoleMenus = sysRoleMenu.getSysRoleMenus();
            Set<Long> collect = sysRoleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
            RoleManger roleManger = new RoleManger();
            BeanUtils.copyProperties(sysRoleMenu, roleManger);
            roleManger.setStatus(sysRoleMenu.getStatus());
            roleManger.setHaveKey(collect);
            mangers.add(roleManger);
        }
        roleMangerRoot.setMangers(mangers);
        roleMangerRoot.setMenuMangerList(menuService.getMenuList());
        return roleMangerRoot;
    }

    private void checkBase(RoleManger roleManger) {
        if (StringUtils.isEmpty(roleManger.getRoleName())) {
            throw new CustomException("角色名称不能为空");
        }
        if (StringUtils.isEmpty(roleManger.getRoleKey())) {
            throw new CustomException("角色标识字符不能为空");
        }
        if (roleManger.getRoleSort() == null) {
            throw new CustomException("角色排序不能为空");
        }
        if (roleManger.getStatus() == null) {
            throw new CustomException("角色状态不能为空");
        }
    }

    private void checkAdd(RoleManger roleManger) {
        checkBase(roleManger);
    }

    private void checkUpdate(RoleManger roleManger) {
        checkBase(roleManger);
        if (roleManger.getId() == null) {
            throw new CustomException("缺少更新对象");
        }
    }

    @Transactional
    @Override
    public String addRole(RoleManger roleManger) {
        checkAdd(roleManger);
        SysRole sysRole = new SysRole();


        BeanUtils.copyProperties(roleManger, sysRole);
        boolean save = iSysRoleService.save(sysRole);
        Set<Long> haveKey = roleManger.getHaveKey();
        iSysRoleMenuService.addListByRoleAndMenuIds(sysRole.getId(), new ArrayList<>(haveKey));
        return "添加成功";
    }

    @Transactional
    @Override
    public String updateRole(RoleManger roleManger) {
        checkUpdate(roleManger);
        //更新role
        LambdaUpdateWrapper<SysRole> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(SysRole::getId, roleManger.getId());
        lambdaUpdateWrapper.set(SysRole::getRoleSort, roleManger.getRoleSort());
        lambdaUpdateWrapper.set(SysRole::getRoleName, roleManger.getRoleName());
        lambdaUpdateWrapper.set(SysRole::getStatus, roleManger.getStatus());
        lambdaUpdateWrapper.set(SysRole::getRoleKey, roleManger.getRoleKey());
        iSysRoleService.update(lambdaUpdateWrapper);
        //删除原来的menu绑定
        LambdaQueryWrapper<SysRoleMenu> sysRoleMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysRoleMenuLambdaQueryWrapper.eq(SysRoleMenu::getRoleId, roleManger.getId());
        iSysRoleMenuService.remove(sysRoleMenuLambdaQueryWrapper);
        //添加新的
        iSysRoleMenuService.addListByRoleAndMenuIds(roleManger.getId(), new ArrayList<>(roleManger.getHaveKey()));
        return "更新成功";
    }

    @Transactional
    @Override
    public String deleteRole(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new CustomException("id不能为空");
        }
        if (id.contains(",")) {
            String[] ids = id.split(",");
            for (String nid :
                    ids
            ) {
                iSysRoleService.removeById(Long.valueOf(nid));
                LambdaQueryWrapper<SysRoleMenu> sysRoleMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
                sysRoleMenuLambdaQueryWrapper.eq(SysRoleMenu::getRoleId, nid);
                iSysRoleMenuService.remove(sysRoleMenuLambdaQueryWrapper);
            }
        } else {
            iSysRoleService.removeById(Long.valueOf(id));
            LambdaQueryWrapper<SysRoleMenu> sysRoleMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysRoleMenuLambdaQueryWrapper.eq(SysRoleMenu::getRoleId, id);
            iSysRoleMenuService.remove(sysRoleMenuLambdaQueryWrapper);
        }
        return "删除成功";
    }

    @Override
    public List<SysRole> list() {
        return iSysRoleService.list();
    }

}
