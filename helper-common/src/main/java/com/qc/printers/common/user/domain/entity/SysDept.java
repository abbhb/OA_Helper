package com.qc.printers.common.user.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门表
 */
@Data
public class SysDept implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门状态:1正常,0停用
     */
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户
     */
    private Long createUser;

    /**
     * 更新用户
     */
    private Long updateUser;
}
