package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 考勤组规则信息
 */
@Data
public class RulesInfo implements Serializable {
    private List<Long> userIds;

    private Integer signinType;

    private List<SigninWay> signinWays;

    /**
     * 考勤时间规则
     */
    private List<KQSJRule> kqsj;

}
