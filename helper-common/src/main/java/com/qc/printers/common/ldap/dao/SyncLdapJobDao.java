package com.qc.printers.common.ldap.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.ldap.domain.entity.SyncLdapJob;
import com.qc.printers.common.ldap.mapper.SyncLdapJobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SyncLdapJobDao extends ServiceImpl<SyncLdapJobMapper, SyncLdapJob> {
}
