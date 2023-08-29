package com.qc.printers.custom.navigation.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class QuickNavigationResult {
    private String id;
    private String name;

    private List<QuickNavigationItemResult> item;
}
