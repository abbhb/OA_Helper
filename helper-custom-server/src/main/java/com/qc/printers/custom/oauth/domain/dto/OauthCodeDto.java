package com.qc.printers.custom.oauth.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OauthCodeDto implements Serializable {
    private Long userId;

    private String redirectUri;
}
