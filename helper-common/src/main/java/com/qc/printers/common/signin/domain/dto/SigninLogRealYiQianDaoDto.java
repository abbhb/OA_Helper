package com.qc.printers.common.signin.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SigninLogRealYiQianDaoDto implements Serializable {
    private String tag;// 请假就写请假，迟到早退都直接标注，最近班次始终只有单班！一个班次里上班下班是分开的

    private String name;

    /**
     * 完整部门名
     */
    private String deptName;

    /**
     * 签到时间
     */
    private String dateTime;


}
