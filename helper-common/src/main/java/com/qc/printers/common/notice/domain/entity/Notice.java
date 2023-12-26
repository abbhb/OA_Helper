package com.qc.printers.common.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Notice implements Serializable {

    private Long id;

    private String title;

    private String content;

    private Integer status;

    /**
     * 1为正常的content模式，2为外联模式
     */
    private Integer type;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic//如果加了这个字段就说明这个表里默认都是假删除，mp自带的删除方法都是改状态为1，默认0是不删除。自定义的mybatis得自己写
    private Integer isDeleted;

    private Integer amount;

    private Integer isAnnex;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    private String tag;

    private Long releaseUser;

    private String releaseUserName;

    private LocalDateTime releaseTime;

    private Long releaseDept;

    private String releaseDeptName;

    private Integer urgency;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Integer version;

    private String updateUserList;

    private Integer visibility;

}
