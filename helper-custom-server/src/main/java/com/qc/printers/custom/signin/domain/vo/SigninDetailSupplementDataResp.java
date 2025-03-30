package com.qc.printers.custom.signin.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SigninDetailSupplementDataResp implements Serializable {
    private Integer index;
    /**
     * 补签项
     */
    private String supplementItem;

    /**
     * 补签理由
     */
    private String supplementReason;

    /**
     *  备注
     */
    private String remarks;

    /**
     * 审批状态
     */
    private String approvalStatus;

    /**
     * 单据来源
     */
    private String source;
}
