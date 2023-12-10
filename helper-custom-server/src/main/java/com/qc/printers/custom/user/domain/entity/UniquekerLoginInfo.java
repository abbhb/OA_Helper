package com.qc.printers.custom.user.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UniquekerLoginInfo implements Serializable {
    private Integer code;

    private String msg;

    private String type;

    private String accessToken;

    /**
     * 第三方UID,当第三方id即可
     */
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
    /**
     * 用户登录IP
     */
    private String ip;
}
