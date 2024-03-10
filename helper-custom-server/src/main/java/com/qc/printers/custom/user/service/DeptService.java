package com.qc.printers.custom.user.service;

import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.custom.user.domain.vo.response.dept.DeptManger;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DeptService {
    List<DeptManger> getDeptList();

    String addDept(DeptManger deptManger);

    String updateDept(DeptManger deptManger);

    String deleteDept(String id);

    List<DeptManger> getDeptListOnlyTree();

    PageData<DeptManger> listForBPM(Integer pageNum, Integer pageSize, String name);
}
