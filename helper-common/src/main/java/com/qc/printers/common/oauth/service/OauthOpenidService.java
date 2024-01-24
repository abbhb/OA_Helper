package com.qc.printers.common.oauth.service;

public interface OauthOpenidService {
    /**
     * 获取一个保证没被该客户端已经使用的openid
     *
     * @param oauthId
     * @return
     */
    Integer getOneOpenidForOauth(Long oauthId);

    /**
     * 是否该客户端包含某个的openid
     *
     * @param openid
     * @param oauthId
     * @return true:是
     */
    boolean isHaveOpenid(Integer openid, Long oauthId);

    boolean isHaveOpenidByUser(Long userId, Long oauthId);

    /**
     * 更新redis
     * 当客户端id不为空时，仅更新该客户端
     */
    void initRedisOpenidMax(Long oauthId);


}
