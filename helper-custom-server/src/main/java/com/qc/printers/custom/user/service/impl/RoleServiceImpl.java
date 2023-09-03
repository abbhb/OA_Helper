package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.service.ISysRoleService;
import com.qc.printers.custom.user.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private ISysRoleService iSysRoleService;

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
}
