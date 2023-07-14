package com.qc.printers.pojo.dto;

import com.qc.printers.pojo.StudyClock;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用于前端用户页信息获取
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClockSelfDTO extends StudyClock {
    //每日是否达标
    private Boolean isStandard;

    //当日是否签到
    private Boolean isSigned;

    //未达标原因
    private String why;

    //月累计时长
    private Double monthTime;

    //按照现有规则本月完成打卡天数
    private Integer integrityDay;

    //当前规则的每天最小完成时间
    private Double minOldTime;

    //当前规则的每天最迟签到时间
    private LocalDateTime maxFirstTime;


}
