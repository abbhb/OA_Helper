package com.qc.printers.custom.navigation.domain.vo.req;

import com.qc.printers.common.navigation.domain.entity.QuickNavigationCategorize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuickNavigationCategorizeUpdateReq implements Serializable {
    private QuickNavigationCategorize quickNavigationCategorize;

    /**
     * 可见部门id列表
     * 当所有人可见时可以为空，否则报错
     */
    private List<Long> visDeptIds;
}
