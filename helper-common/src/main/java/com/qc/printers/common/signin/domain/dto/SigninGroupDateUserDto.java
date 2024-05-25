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
     * 该用户详细的logList
     */
    private List<SigninLogCliBcDto> logList;
}
