package com.qc.printers.common.oauth.service.impl;

import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.entity.SysOauthUser;
import com.qc.printers.common.oauth.service.OauthUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OauthUserServiceImpl implements OauthUserService {
    @Override
    public List<SysOauth> listMyOauth() {
        return null;
    }

    @Override
    public String cancelOauthAuthorization(Long id) {
        return null;
    }

    @Override
    public String agreeOauthAuthorization(SysOauthUser sysOauthUser, List<SysOauthUser> permissions) {
        return null;
    }

    @Override
    public String checkOauthAuthorization(Long userId, Long oauthId) {
        return null;
    }
}
