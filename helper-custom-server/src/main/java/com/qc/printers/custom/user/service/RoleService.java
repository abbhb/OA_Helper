package com.qc.printers.custom.user.service;

import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.custom.user.domain.vo.response.role.RoleManger;
import com.qc.printers.custom.user.domain.vo.response.role.RoleMangerRoot;

import java.util.List;

public interface RoleService {
    List<String> getroleNameByKey(String key);

    RoleMangerRoot getRoleList();

    String addRole(RoleManger roleManger);

    String updateRole(RoleManger roleManger);

    String deleteRole(String id);

    List<SysRole> list();
}
