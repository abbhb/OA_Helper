package com.qc.printers.common.oauth.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * oauth提供者勾选的需要的权限配置
 */
@Data
public class SysOauthPermissionBind implements Serializable {
    private Long id;

    /**
     * oauth表的id
     */
    private Long oauthId;

    private Long oauthPermissionId;
}
