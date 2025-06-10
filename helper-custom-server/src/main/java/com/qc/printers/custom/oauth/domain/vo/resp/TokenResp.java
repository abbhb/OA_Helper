package com.qc.printers.custom.oauth.domain.vo.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class TokenResp implements Serializable {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    //秒
    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * OIDC ID Token
     * JWT格式，包含用户身份信息
     */
    @JsonProperty("id_token")
    private String idToken;

    /**
     * 当异常时才会返回以下字段
     */
    private Integer code;

    private String msg;
}
