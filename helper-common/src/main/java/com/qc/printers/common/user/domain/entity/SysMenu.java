package com.qc.printers.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysMenu implements Serializable {
    private Long id;

    /**
     * 菜单名称
     */
    private String name;

    private String locale;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 路由地址
     */
    private String path;


    /**
     * 是否为外链
     * 1：是
     * 0：否
     */
    private Integer isFrame;

    /**
     * 是否缓存
     * （1缓存 0不缓存）
     */
    private Integer isCache;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String type;
    /**
     * 是否展示
     * （1展示 0不展示）
     */
    private Integer isShow;

    private Integer status;

    /**
     * 权限字符串
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT)

    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
