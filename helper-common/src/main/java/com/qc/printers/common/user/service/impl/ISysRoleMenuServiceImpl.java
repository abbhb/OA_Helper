package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.domain.entity.SysRoleMenu;
import com.qc.printers.common.user.mapper.SysRoleMenuMapper;
import com.qc.printers.common.user.service.ISysRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ISysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements ISysRoleMenuService {
    @Transactional
    @Override
    public boolean addListByRoleAndMenuIds(Long roleId, List<Long> menuIds) {
        if (roleId == null) {
            throw new CustomException("错误");
        }
        for (Long aLong : menuIds) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenu.setMenuId(aLong);
            this.save(sysRoleMenu);
        }
        return true;
    }
}
