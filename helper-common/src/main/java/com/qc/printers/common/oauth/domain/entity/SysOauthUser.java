package com.qc.printers.common.oauth.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysOauthUser implements Serializable {
    private Long id;

    private Long oauthId;

    private Long userId;

    /**
     * 可选，授权如果包含新增的或者以前没授权过额外权限就前端再次确认，否则直接进入，无需确认
     */
    private String scope;
}

