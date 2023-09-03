package com.qc.printers.custom.user.domain.vo.response.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MenuManger implements Serializable {
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
    private Integer sort;

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

    private Long createUser;

    private String createUserName;

    private LocalDateTime createTime;

    private Long updateUser;

    private String updateUserName;

    private LocalDateTime updateTime;

    private List<MenuManger> children;
}
