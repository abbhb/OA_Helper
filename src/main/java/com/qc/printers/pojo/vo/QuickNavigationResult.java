package com.qc.printers.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class QuickNavigationResult {
    private String id;
    private String name;

    private List<QuickNavigationItemResult> item;
}
