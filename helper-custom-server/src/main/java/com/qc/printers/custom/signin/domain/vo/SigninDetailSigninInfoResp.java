package com.qc.printers.custom.signin.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *  // 打卡记录接口定义
 *   interface PunchRecord {
 *     scheduledPunchTime: string; // 应打卡时间
 *     actualPunchTime: string; // 实打卡时间
 *     absentDuration: string; // 缺勤时长
 *     supplementPoint: string; // 补签点
 *     supplementStatus: string; // 补签状态
 *     supplementReason: string; // 补签理由
 *     attendanceStatus: string; // 考勤状态
 *     supplementApplyTime: string; // 补签申请时间
 *     supplementApprovalTime: string; // 补签审批时间
 *   }
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SigninDetailSigninInfoResp implements Serializable {
    // 排序
    private Integer index;
    // 应打卡时间 例如：2024-03-16 12:00:00
    private String scheduledPunchTime;
    // 实打卡时间 例如：2024-03-16 12:00:00
    private String actualPunchTime;
    // 缺勤时长 例如：40分钟
    private String absentDuration;

    // 考勤状态 例如：正常、迟到、早退、缺勤
    private String attendanceStatus;

    // 补签点 例如：2024-03-16 12:00:00
    private String supplementPoint;
    // 补签状态 例如：待审批、已通过、已拒绝
    private String supplementStatus;
    // 补签理由 例如：迟到、早退、漏打卡
    private String supplementReason;
    // 补签申请时间 2024-03-16 12:00:00
    private String supplementApplyTime;
    // 补签审批时间 2024-03-16 12:00:00
    private String supplementApprovalTime;
}
