package com.qc.printers.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.user.domain.entity.SysRoleMenu;

import java.util.List;

public interface ISysRoleMenuService extends IService<SysRoleMenu> {
    boolean addListByRoleAndMenuIds(Long roleId, List<Long> menuIds);
}
