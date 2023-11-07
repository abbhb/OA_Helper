package com.qc.printers.common.oauth.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.oauth.domain.entity.SysOauthUser;
import com.qc.printers.common.oauth.mapper.SysOauthUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Slf4j
public class SysOauthUserDao extends ServiceImpl<SysOauthUserMapper, SysOauthUser> {

}
