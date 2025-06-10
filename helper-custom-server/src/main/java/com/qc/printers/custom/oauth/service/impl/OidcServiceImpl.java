package com.qc.printers.custom.oauth.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qc.printers.custom.oauth.domain.vo.resp.OauthUserInfoResp;
import com.qc.printers.custom.oauth.service.OauthService;
import com.qc.printers.custom.oauth.service.OidcService;
import com.qc.printers.custom.oauth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OidcServiceImpl implements OidcService {

    @Autowired
    private OauthService oauthService;
    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        // TODO: 根据你的业务逻辑实现用户信息获取
        // 这里需要你实现：
        // 1. 验证 access_token 的有效性
        // 2. 从你的用户系统中获取用户信息
        // 3. 返回符合 OIDC 标准的用户信息字段
        OauthUserInfoResp userInfoOnlyAccessToken = oauthService.getUserInfoOnlyAccessToken(accessToken);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", String.valueOf(userInfoOnlyAccessToken.getId()));
        userInfo.put("id", String.valueOf(userInfoOnlyAccessToken.getId()));
        userInfo.put("name", userInfoOnlyAccessToken.getName());
        userInfo.put("nickname",userInfoOnlyAccessToken.getNickname());
        userInfo.put("email",userInfoOnlyAccessToken.getEmail());
        userInfo.put("sex", userInfoOnlyAccessToken.getSex());
        userInfo.put("avatar", userInfoOnlyAccessToken.getAvatar());
        userInfo.put("username", userInfoOnlyAccessToken.getUsername());

        return userInfo;
    }

} 