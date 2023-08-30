package com.qc.printers.common.user.domain.entity;

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
     * 组件地址
     */
    private String component;

    /**
     * 路由参数
     */
    private String query;

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

    private Integer visible;

    private Integer status;

    /**
     * 权限字符串
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    private Long createUser;

    private LocalDateTime createTime;

    private Long updateUser;

    private LocalDateTime updateTime;

}
