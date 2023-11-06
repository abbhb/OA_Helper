package com.qc.printers.common.oauth.domain.dto;

import com.qc.printers.common.oauth.domain.entity.SysOauth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class SysOauthInfoDto extends SysOauth implements Serializable {
    List<SysOauthPermissionBindInfoDto> infos;
}
