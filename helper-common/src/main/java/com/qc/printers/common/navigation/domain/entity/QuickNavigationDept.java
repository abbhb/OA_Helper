package com.qc.printers.common.navigation.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuickNavigationDept implements Serializable {
    private Long id;

    private Long quickNavCategorizeId;

    /**
     * 绑定部门可见
     */
    private Long deptId;
}
