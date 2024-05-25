package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SigninLogAskLeave implements Serializable {
    private Long id;

    private Long bcId;

    private Integer bcCount;

    private Long userId;

    private LocalDate date;

    private Long createUser;

    private LocalDateTime createTime;
}
