package com.qc.printers.common.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.user.domain.dto.RoleMenuDTO;
import com.qc.printers.common.user.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<RoleMenuDTO> getAllSysRoleMenu();
}
