package com.qc.printers.common.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.user.domain.entity.Permission;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
