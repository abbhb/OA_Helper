package com.qc.printers.common.ldap.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.ldap.domain.entity.SyncLdapJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SyncLdapJobMapper extends BaseMapper<SyncLdapJob> {
}
