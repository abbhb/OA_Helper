package com.qc.printers.custom.user.service;

import com.qc.printers.custom.user.domain.vo.response.dept.DeptManger;

import java.util.List;

public interface DeptService {
    List<DeptManger> getDeptList();

    String addDept(DeptManger deptManger);

    String updateDept(DeptManger deptManger);

    String deleteDept(String id);

    List<DeptManger> getDeptListOnlyTree();
}
