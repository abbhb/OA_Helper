package com.qc.printers.common.signin.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户申请补签
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SigninRenewal implements Serializable {

    private Long id;


    private Long userId;

    private LocalDateTime renewalTime;

    private LocalDateTime createTime;


    private String renewalReason;

    private String renewalAboutActId;
    private String signinLogId;


}
