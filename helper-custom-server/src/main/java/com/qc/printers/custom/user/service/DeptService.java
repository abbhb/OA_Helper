package com.qc.printers.custom.user.service;

import com.qc.printers.common.user.domain.dto.DeptManger;

import java.util.List;

public interface DeptService {
    List<DeptManger> getDeptList();

    String addDept(DeptManger deptManger);

    String updateDept(DeptManger deptManger);

    String deleteDept(String id);

    List<DeptManger> getDeptListOnlyTree();

    List<DeptManger> listForBPM(String name);
}
