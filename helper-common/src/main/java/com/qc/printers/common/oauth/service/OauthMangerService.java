package com.qc.printers.common.oauth.service;

import com.qc.printers.common.oauth.domain.entity.SysOauth;

import java.util.List;

public interface OauthMangerService {

    /**
     * 管理系统添加oauth
     *
     * @param sysOauth
     * @return
     */
    String addOauth(SysOauth sysOauth);

    /**
     * oauth删除，会同步删除权限的绑定
     *
     * @param id oauth表的绑定
     * @return
     */
    String deleteOauth(Long id);

    /**
     * 更新oauth
     *
     * @param sysOauth
     * @return
     */
    String updateOauth(SysOauth sysOauth);

    /**
     * @param sysOauth 只用传入id和status
     * @return
     */
    String changeOauthStatus(SysOauth sysOauth);

    List<SysOauth> listOauth();

    SysOauth queryOauth(Long oauthId);

    void userAgree(Long oauthId, Long userId, String scope);


}
