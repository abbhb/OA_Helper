package com.qc.printers.custom.oauth.domain.vo.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OauthUserInfoResp implements Serializable {
    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;


    /**
     * 当异常时才会返回以下字段
     */
    private Integer code;

    private String msg;
}
