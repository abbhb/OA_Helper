package com.qc.printers.common.signin.domain.dto;

import com.qc.printers.common.signin.domain.entity.SigninLogCli;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 组考勤情况
 * 用户具体情况
 */
@Data
public class SigninGroupDateUserDto implements Serializable {
    private Long userId;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 该用户的部门id
     */
    private Long deptId;

    /**
     * 完整部门链路名
     */
    private String deptName;

    /**
     * 应该有的班次
     */
    private Integer bcCount;

    /**
     * 昨天概况状态，缺勤或者正常，异常为存在迟到或者早退
     * 根据所有班次的状态综合，全出勤为正常，迟到和早退存在但不存在缺勤就是异常，全正常为正常
     */
    private Integer state;

    /**
     * 该用户详细的logList
     */
    private List<SigninLogCliBcDto> logList;
}
