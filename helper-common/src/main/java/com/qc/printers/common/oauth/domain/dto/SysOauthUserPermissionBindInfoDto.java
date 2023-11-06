package com.qc.printers.common.oauth.domain.dto;

import com.qc.printers.common.oauth.domain.entity.SysOauthPermission;
import com.qc.printers.common.oauth.domain.entity.SysOauthUserPermissionBind;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 用户权限绑定带详情
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class SysOauthUserPermissionBindInfoDto extends SysOauthUserPermissionBind {
    private SysOauthPermission info;
}
