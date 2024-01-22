package com.qc.printers.custom.oauth.domain.vo.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qc.printers.common.oauth.domain.dto.OauthBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class OauthUserInfoResp extends OauthBase implements Serializable {

    /**
     * 此id其实返回的是openid，返回给三方的不可能给真实id
     */
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

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

}
