package com.qc.printers.common.oauth.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 第三方关联表
 */
@Data
public class SysOauthOpenid implements Serializable {

    private Long id;

    private Long sysOauthId;

    private Long userId;

    private Integer openid;
}
