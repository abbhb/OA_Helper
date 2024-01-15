package com.qc.printers.custom.navigation.domain.vo;

import lombok.Data;

@Data
public class QuickNavigationItemResult {
    private String id;
    private String name;
    private String path;
    private String image;
    private String introduction;

    private String categorizeId;

    private String categorizeName;

    /**
     * 0:url
     * 1:md
     */
    private Integer type;

    private String content;
}
