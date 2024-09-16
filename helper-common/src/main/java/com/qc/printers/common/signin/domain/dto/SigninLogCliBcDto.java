package com.qc.printers.common.signin.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 专门为统计数据降低粒度
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SigninLogCliBcDto implements Serializable {

    public SigninLogCliBcDto(Long userId, LocalDate logDatetime, Integer bcCount) {
        this.userId = userId;
        this.logDatetime = logDatetime;
        this.bcCount = bcCount;
    }

    private Long userId;
    /**
     * 哪一天的记录
     */
    private LocalDate logDatetime;

    /**
     * 一个班次的聚合状态 所以此处不会出现1，2，只能概括为5
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
     * 上班item
     */
    private SigninLogCliBcItem sbItem = new SigninLogCliBcItem();

    /**
     * 下班item
     */
    private SigninLogCliBcItem xbItem = new SigninLogCliBcItem();
}


