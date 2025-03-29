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

    /**
     * 补签发起时间，在流程开始时创建
     */
    private LocalDateTime createTime;


    private LocalDateTime updateTime;

    /**
     * 补签审批时间 approval_time
     * 可能 Null
     */
    private LocalDateTime approvalTime;

    private String renewalReason;

    private String renewalAboutActId;
    private String signinLogId;

    /**
     * 当前补签状态，1为通过，0为流程中，2为拒绝
     */
    private Integer state;


}
