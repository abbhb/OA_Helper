package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户申请补签
 */
@Data
public class SigninRenewal implements Serializable {

    private Long id;


    private Long userId;

    private LocalDateTime renewalTime;

    private LocalDateTime createTime;


    private String renewalReason;

    private String renewalAboutActId;


}
