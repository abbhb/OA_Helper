package com.qc.printers.common.oauth.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.oauth.domain.entity.SysOauthUserPermissionBind;
import com.qc.printers.common.oauth.mapper.SysOauthUserPermissionBindMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SysOauthUserPermissionBindDao extends ServiceImpl<SysOauthUserPermissionBindMapper, SysOauthUserPermissionBind> {

}
