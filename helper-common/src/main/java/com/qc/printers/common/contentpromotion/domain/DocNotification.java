package com.qc.printers.common.contentpromotion.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocNotification implements Serializable {
    private Long id;
    private String title;
    /**
     * 程度
     * 1：不急  2：一般  3：重要
     */
    private Integer type;

    /**
     * 内容
     * 基于md内容
     */
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;

    /**
     * 分类id
     */
    private Long docClassificationId;

    /**
     * 可见度
     * 0表示全都可见（无需密码）
     * 1表示包含仅哪些部门可见（密码也无效）
     * 2表示包含的部门可见，不可见可通过密码
     * 3表示预发布，所有人可见，但都要密码
     */
    private Integer seeType;

    private String password;
}
