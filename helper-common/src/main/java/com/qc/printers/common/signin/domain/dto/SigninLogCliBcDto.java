package com.qc.printers.common.signin.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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


    // 请假id（如果有）标记为不序列化
    private transient List<Long> askLeaveId;

    public void calculateState() {
        // 如果上班和下班都是正常(0)，则整体状态为正常(0)
        if (sbItem.getState().equals(0) && xbItem.getState().equals(0)) {
            state = 0;
        }
        // 如果上班或下班有请假(4)，则整体状态为请假(4)
        else if (sbItem.getState().equals(4) || xbItem.getState().equals(4)) {
            state = 4;
        }
        // 如果上班或下班有缺勤(3)，则整体状态为缺勤(3)
        else if (sbItem.getState().equals(3) || xbItem.getState().equals(3)) {
            state = 3;
        }
        // 如果上班有迟到(1)或下班有早退(2)，则整体状态为有迟到早退但不算缺勤(5)
        else if (sbItem.getState().equals(1) || xbItem.getState().equals(2) || 
                sbItem.getState().equals(2) || xbItem.getState().equals(1)) {
            state = 5;
        }
        // 其他情况，整体状态为缺勤(3)
        else {
            state = 3;
        }
    }
}


