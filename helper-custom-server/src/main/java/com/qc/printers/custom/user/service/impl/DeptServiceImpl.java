package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.user.domain.vo.response.dept.DeptManger;
import com.qc.printers.custom.user.service.DeptService;
import com.qc.printers.custom.user.utils.DeptMangerHierarchyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private ISysDeptService iSysDeptService;


    @Override
    public List<DeptManger> getDeptList() {
        List<SysDept> list = iSysDeptService.list();
        list.sort((m1, m2) -> {
            Long order1 = m1.getId();
            Long order2 = m2.getId();
            return order1.compareTo(order2);
        });
        if (list.size() < 1) {
            throw new CustomException("系统异常，请添加根节点(id:0)");
        }

        DeptMangerHierarchyBuilder deptMangerHierarchyBuilder = new DeptMangerHierarchyBuilder(list);
        List<DeptManger> deptMangers = deptMangerHierarchyBuilder.buildHierarchy();
        sortRecursion(deptMangers);
        return deptMangers;
    }

    private void sortRecursion(List<DeptManger> deptMangers) {
        deptMangers.sort((m1, m2) -> {
            Integer order1 = m1.getOrderNum();
            Integer order2 = m2.getOrderNum();
            return order1.compareTo(order2);
        });
        for (DeptManger deptManger :
                deptMangers) {
            if (deptManger.getChildren() != null) {
                if (deptManger.getChildren().size() > 0) {
                    sortRecursion(deptManger.getChildren());
                }
            }
        }
    }


    @Transactional
    @Override
    public String addDept(DeptManger deptManger) {
        if (StringUtils.isEmpty(deptManger.getDeptName())) {
            throw new CustomException("请输入DeptName");
        }
        if (deptManger.getParentId() == null) {
            throw new CustomException("父级ID不能为空");
        }
        if (StringUtils.isEmpty(deptManger.getStatus())) {
            deptManger.setStatus("1");
        }
        SysDept sysDept = new DeptManger();
        BeanUtils.copyProperties(deptManger, sysDept);
        sysDept.setId(null);
        iSysDeptService.save(sysDept);
        return "添加成功";
    }

    @Transactional
    @Override
    public String updateDept(DeptManger deptManger) {
        if (deptManger.getId() == null) {
            throw new CustomException("无操作对象");
        }
        if (StringUtils.isEmpty(deptManger.getDeptName())) {
            throw new CustomException("请输入DeptName");
        }
        if (deptManger.getParentId() == null) {
            throw new CustomException("父级ID不能为空");
        }
        if (StringUtils.isEmpty(deptManger.getStatus())) {
            deptManger.setStatus("1");
        }
        LambdaUpdateWrapper<SysDept> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(SysDept::getId, deptManger.getId());
        lambdaUpdateWrapper.set(SysDept::getDeptName, deptManger.getDeptName());
        lambdaUpdateWrapper.set(SysDept::getEmail, deptManger.getEmail());
        // 前面的级别逗号分隔，前端传入
        lambdaUpdateWrapper.set(SysDept::getAncestors, deptManger.getAncestors());
        lambdaUpdateWrapper.set(SysDept::getParentId, deptManger.getParentId());
        lambdaUpdateWrapper.set(SysDept::getLeader, deptManger.getLeader());
        lambdaUpdateWrapper.set(SysDept::getOrderNum, deptManger.getOrderNum());
        lambdaUpdateWrapper.set(SysDept::getStatus, deptManger.getStatus());
        lambdaUpdateWrapper.set(SysDept::getPhone, deptManger.getPhone());
        iSysDeptService.update(lambdaUpdateWrapper);
        return "更新成功";
    }

    @Transactional
    @Override
    public String deleteDept(String id) {
        //需要确保该部门以及下属部门没有绑定用户
        //符合条件进行级联 删除(和菜单逻辑类似)
        return null;
    }


}
