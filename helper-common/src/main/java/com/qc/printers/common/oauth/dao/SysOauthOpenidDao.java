package com.qc.printers.common.oauth.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.oauth.domain.entity.SysOauthOpenid;
import com.qc.printers.common.oauth.mapper.SysOauthOpenidMapper;
import org.springframework.stereotype.Service;

@Service
public class SysOauthOpenidDao extends ServiceImpl<SysOauthOpenidMapper, SysOauthOpenid> {
}
