package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class SigninLogCli implements Serializable {
    private Long id;
    private Long userId;

    /**
     * 哪一天的记录
     */
    private LocalDate logDatetime;

    /**
     * 状态,0正常，1为迟到，2为早退，3为缺勤，记录不存在在查询的时候创建缺勤记录
     */
    private Integer state;

    /**
     * 第几个班次
     */
    private Integer bcCount;
    /**
     * 0为上班，1为下班
     */
    private Integer startEnd;
    /**
     * 当天具体签到时间字符串
     */
    private String logTime;
    /**
     * 来源原始记录
     */
    private String fromLog;
    /**
     * 状态时间，例如早退多少分钟
     */
    private Integer stateTime;
}
