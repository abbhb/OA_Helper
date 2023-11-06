package com.qc.printers.common.oauth.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * oauth授权的用户勾选的
 */
@Data
public class SysOauthUserPermissionBind implements Serializable {
    private Long id;

    /**
     * 注意此处不是userid而是sys_oauth_user表的id
     */
    private Long oauthUserId;

    private Long oauthId;

    private Long oauthPermissionId;
}
