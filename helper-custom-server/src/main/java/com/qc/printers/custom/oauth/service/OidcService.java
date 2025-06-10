package com.qc.printers.custom.oauth.service;

import java.util.Map;

public interface OidcService {
    /**
     * 获取用户信息
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    Map<String, Object> getUserInfo(String accessToken);


} 