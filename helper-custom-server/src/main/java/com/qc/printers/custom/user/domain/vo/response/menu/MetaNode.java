package com.qc.printers.custom.user.domain.vo.response.menu;

import lombok.Data;

import java.io.Serializable;

@Data
public class MetaNode implements Serializable {
    private String locale;
    private String icon;
    private Integer order;
    private boolean isShow;
    private boolean isFrame;

    /**
     * 额外，标记菜单类型
     * M目录 C菜单 F按钮
     */
    private String type;
}
