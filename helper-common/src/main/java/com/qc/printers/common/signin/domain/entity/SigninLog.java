package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SigninLog implements Serializable {
    private String id;

    private Long userId;

    private String studentId;

    private LocalDateTime signinTime;

    private String signinDeviceId;

    /**
     * 签到方式
     * card
     * face
     * system
     * renewal
     */
    private String signinWay;

    private String signinImage;
    private String signinCardId;

    private Long updateUser;
    private String remark;

}
