package com.qc.printers.common.oauth.service;

import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.entity.SysOauthUser;

import java.util.List;

public interface OauthUserService {
    List<SysOauth> listMyOauth();

    /**
     * 取消授权
     *
     * @param id oauth表的id
     * @return
     */
    String cancelOauthAuthorization(Long id);


    /**
     * 同意授权必须包含最少的必选，不然不给统一，有新的必选没选也不算，得在执行该条，
     *
     * @param sysOauthUser
     * @return
     */
    String agreeOauthAuthorization(SysOauthUser sysOauthUser, List<SysOauthUser> permissions);

    /**
     * 有新的必选没选也不算通过，不通过直接抛出oauth专用异常，跳转到授权页面
     *
     * @param userId
     * @param oauthId
     * @return
     */
    String checkOauthAuthorization(Long userId, Long oauthId);


}
