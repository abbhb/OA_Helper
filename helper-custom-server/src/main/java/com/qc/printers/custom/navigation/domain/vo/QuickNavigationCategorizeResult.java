package com.qc.printers.custom.navigation.domain.vo;

import com.qc.printers.common.user.domain.entity.SysDept;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuickNavigationCategorizeResult implements Serializable {
    private String id;
    private String name;

    /**
     * 可见性
     * 0为都可见
     * 1为仅部门可见
     */
    private Integer visibility;

    /**
     * 当仅部门可见时返回
     */
    private List<SysDept> depts;
}
