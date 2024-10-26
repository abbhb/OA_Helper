package com.qc.printers.custom.user.service;

import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.common.user.domain.entity.SysDept;

import java.util.List;

public interface DeptService {
    List<DeptManger> getDeptList(SysDept sysDept);

    String addDept(DeptManger deptManger);

    String updateDept(DeptManger deptManger);

    String deleteDept(String id);

    List<DeptManger> getDeptListOnlyTree();

    List<DeptManger> listForBPM(String name);

    /**
     * @param deptId 涉及到的部门id
     * 部门表全表扫描逐行进行递归查询祖籍关系，修复信息（慎用，尽量业务量小的时候执行）
     */
    void fixDataWithParent(Long deptId);
}
