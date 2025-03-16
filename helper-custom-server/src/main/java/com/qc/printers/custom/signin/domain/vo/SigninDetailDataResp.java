package com.qc.printers.custom.signin.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SigninDetailDataResp implements Serializable {
    // 日期
    private String date;
    // 工时
    private String workingHours;
    // 签到状态
    private String attendanceStatus;
    // 补签状态
    private String supplementStatus;
    // 缺勤时长
    private String durationOfAbsence;
    // 备注，例如请假[请假原因][时间段]
    private String remarks;
}
