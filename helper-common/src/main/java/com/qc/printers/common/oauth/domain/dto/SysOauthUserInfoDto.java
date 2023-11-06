package com.qc.printers.common.oauth.domain.dto;

import com.qc.printers.common.oauth.domain.entity.SysOauthUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class SysOauthUserInfoDto extends SysOauthUser implements Serializable {
    private SysOauthUserPermissionBindInfoDto infos;
}
