package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SigninLog implements Serializable {
    private Long id;

    private Long userId;

    private String studentId;

    private LocalDateTime signinTime;

    private String signinDeviceId;
}
