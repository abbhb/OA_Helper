package com.qc.printers.common.oauth.service;

import com.qc.printers.common.oauth.domain.dto.SysOauthInfoDto;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.entity.SysOauthPermission;
import com.qc.printers.common.oauth.domain.entity.SysOauthPermissionBind;

import java.util.List;

public interface OauthMangerService {
    String addOauthPermission(SysOauthPermission sysOauthPermission);

    /**
     * @param sysOauthPermission
     * @return
     */
    String updateOauthPermission(SysOauthPermission sysOauthPermission);

    /**
     * 有oauth绑定的情况下禁止移除，返回哪些还在使用
     *
     * @param id
     * @return
     */
    List<SysOauthInfoDto> deleteOauthPermission(Long id);

    SysOauthPermission getOauthPermission(Long id);

    List<SysOauthPermission> listOauthPermission();

    /**
     * 管理系统添加oauth
     *
     * @param sysOauth
     * @param sysOauthPermissionBinds
     * @return
     */
    String addOauth(SysOauth sysOauth, List<SysOauthPermissionBind> sysOauthPermissionBinds);

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
     * @param sysOauthPermissionBinds
     * @return
     */
    String updateOauth(SysOauth sysOauth, List<SysOauthPermissionBind> sysOauthPermissionBinds);

    /**
     * @param sysOauth 只用传入id和status
     * @return
     */
    String changeOauthStatus(SysOauth sysOauth);

    List<SysOauthInfoDto> listOauth();

    SysOauthInfoDto queryOauth();


}
