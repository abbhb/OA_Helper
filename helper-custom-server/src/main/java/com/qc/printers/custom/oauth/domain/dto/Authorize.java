package com.qc.printers.custom.oauth.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 传参使用，非实体类
 */
@Data
public class Authorize implements Serializable {
    //authorizeCode
    private String code;

    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("response_type")
    private String responseType;

    @JsonProperty("redirect_uri")
    private String redirectUri;


    /**
     * 获取身份验证代码和获取访问令牌的客户端的标识符
     */
    @JsonProperty("client_id")
    private String clientId;


    /**
     * 用于获取令牌时校验
     */
    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("refresh_token")
    private String refreshToken;

}
