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
public class SigninDetailClockingDataResp implements Serializable {
    /**
     * 排序
     */
    private Integer index;
    /**
     * 员工信息
     */
    private Employee employee;
    
    /**
     * 打卡时间
     */
    private String punchTime;
    
    /**
     * 考勤卡号
     */
    private String attendanceCard;
    
    /**
     * 考勤来源
     */
    private String signinOrigin;

    /**
     * 考勤来源详情
     */
    private String signinOriginDetail;

    /**
     * 签到地点
     */
    private String locationInfo;
    
    /**
     * 签到地点详情
     */
    private String locationDescription;
    
    /**
     * 设备标识
     */
    private String deviceInfo;
    
    /**
     * 完整部门名
     */
    private String department;

    /**
     * 创建时间
     */
    private String creationTime;
    
    /**
     * 员工信息内部类
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Employee implements Serializable {
        /**
         * 头像
         */
        private String avatar;
        
        /**
         * 姓名
         */
        private String name;
    }
}
