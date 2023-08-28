package com.qc.printers.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("`group`")
public class Group implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * name
     */
    private String name;


    /**
     * 创建用户
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 假删除
     */
    @TableLogic
    private Integer isDeleted;
}
