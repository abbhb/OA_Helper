package com.qc.printers.common.signin.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 专门为统计数据降低粒度
 */
@Data
public class SigninLogCliBcDto implements Serializable {

    private Long userId;
    /**
     * 哪一天的记录
     */
    private LocalDate logDatetime;

    /**
     * 状态,0正常，1为迟到，2为早退
     * ext- 以下字段为库里没有，但是业务层使用
     * 0 正常
     * 3 为缺勤
     * 4 为请假的记录
     * 5 为上下班有迟到早退但不算缺勤的时候
     */
    private Integer state;

    /**
     * 第几个班次
     */
    private Integer bcCount;

    /**
     * 当天具体签到时间字符串,当state为5，这两个时间不为0就是sb迟到或者下班早退分钟,缺勤或者请假都不展示这两个字段
     */
    private String sblogTime;

    private String xblogTime;

    /**
     * 状态时间，例如早退多少分钟
     */
    private Integer xbzaotui;
    private Integer sbchidao;
}
