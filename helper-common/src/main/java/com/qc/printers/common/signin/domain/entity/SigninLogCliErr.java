package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SigninLogCliErr implements Serializable {
    private Long id;

    private Long signinLogCliId;

    private Integer newState;
    /**
     * 变更理由
     */
    private String reason;

    private Long updateUser;

    private LocalDateTime updateTime;
}
