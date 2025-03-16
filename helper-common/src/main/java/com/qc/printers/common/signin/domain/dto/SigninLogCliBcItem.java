package com.qc.printers.common.signin.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SigninLogCliBcItem implements Serializable {
    /**
     * 应打卡时间
     */
    private LocalDateTime timeY;

    /**
     * 实际打卡时间
     */
    private LocalDateTime timeS;

    /**
     * 一个班次的具体上班或下班状态 所以此处不会出现5，只会出现1，2
     * 状态,0正常，1为迟到，2为早退
     * ext- 以下字段为库里没有，但是业务层使用
     * 0 正常
     * 3 为缺勤
     * 4 为请假的记录
     * 5 为上下班有迟到早退但不算缺勤的时候
     */
    private Integer state;

    /**
     * 是否存在补签,不论状态
     */
    private Boolean bq;

    private Long bqId;


    /**
     * 补签状态
     * 如果存在补签
     * 1为成功
     * 0为流程中
     * 2为失败
     * 如果有多次针对相同班次相同项补签 那就展示最终生效的且最后结束流程的
     */
    private Integer bqState;

    /**
     * 补签点时间，如果有，与补签状态一样，多个的话规则一致
     */
    private LocalDateTime bqTime;

    /**
     * 缺勤多少分钟，早退或者迟到
     */
    private Integer queQingTime;


}
