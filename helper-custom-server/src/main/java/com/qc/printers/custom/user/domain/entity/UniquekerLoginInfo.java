package com.qc.printers.custom.user.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UniquekerLoginInfo implements Serializable {
    private Integer code;

    private String msg;

    private String type;

    @JsonProperty(value = "access_token")
    private String accessToken;

    /**
     * 第三方UID,当第三方id即可
     */
    @JsonProperty(value = "social_uid")
    private String socialUid;

    /**
     * 用户头像
     */
    private String faceimg;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户所在地
     */
    private String location;


    private String gender;
    /**
     * 用户登录IP
     */
    private String ip;
}
