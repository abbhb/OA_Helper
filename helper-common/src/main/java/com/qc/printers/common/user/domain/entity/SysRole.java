package com.qc.printers.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.qc.printers.common.user.domain.enums.DataScopeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String roleName;

    private String roleKey;
    /**
     * @see DataScopeEnum
     */
    private Integer dataScope;

    private Integer roleSort;

    private Integer status;


    //deleted
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
