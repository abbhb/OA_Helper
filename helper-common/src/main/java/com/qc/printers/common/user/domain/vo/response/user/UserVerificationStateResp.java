package com.qc.printers.common.user.domain.vo.response.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserVerificationStateResp implements Serializable {
    /**
     * 实名认证状态
     * 0:未认证
     * 1:已认证
     * 2:审批中
     */
    private Integer state;

    /**
     * 所属用户
     */
    private Long userId;
}
