package com.qc.printers.common.oauth.domain.dto;

import com.qc.printers.common.oauth.domain.entity.SysOauthPermission;
import com.qc.printers.common.oauth.domain.entity.SysOauthPermissionBind;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class SysOauthPermissionBindInfoDto extends SysOauthPermissionBind implements Serializable {

    //详细信息
    private SysOauthPermission info;

}
