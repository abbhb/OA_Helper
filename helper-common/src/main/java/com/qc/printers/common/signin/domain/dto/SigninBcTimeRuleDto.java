package com.qc.printers.common.signin.domain.dto;

import com.qc.printers.common.signin.domain.entity.BcRule;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

@Data
public class SigninBcTimeRuleDto implements Serializable {

    // 具体是哪个班次规则
    private BcRule bcRule;

    /**
     * 返回的校验后的班次状态
     * 0 处于某个班次打卡
     * 1 不处于任何班次任何时间段找得到最近的某个上下班
     * 2 不处于任何班次任何时间段且找不到任何最近的上下班，一般发生在还没到某天第一个打卡段之前
     */
    private Integer state;

    /**
     * 返回的校验后的班次状态
     * 0 上班段
     * 1 下班段
     */
    private Integer sxBState;

    /**
     * 下面字段都是在上下班才有
     * 直接计算好起始时间
     */
    private String startTime;

    private String endTime;



}
