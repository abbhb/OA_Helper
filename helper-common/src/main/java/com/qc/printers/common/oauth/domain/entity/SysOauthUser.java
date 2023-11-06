package com.qc.printers.common.oauth.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysOauthUser implements Serializable {
    private Long id;

    private Long oauthId;

    private Long userId;
}
