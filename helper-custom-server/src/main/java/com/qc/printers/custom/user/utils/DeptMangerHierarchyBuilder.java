package com.qc.printers.custom.user.utils;

import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.SysRoleDept;
import com.qc.printers.custom.user.domain.vo.response.dept.DeptManger;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DeptMangerHierarchyBuilder {
    private List<SysDept> nodeList;
    private Map<Long, List<DeptManger>> childMap;

    private Set<SysRoleDept> sysRoleDepts;
    private Set<SysRole> sysRoles;

    public DeptMangerHierarchyBuilder(List<SysDept> nodeList, Set<SysRole> sysRoles, Set<SysRoleDept> sysRoleDepts) {
        this.nodeList = nodeList;
        this.childMap = new HashMap<>();
        this.sysRoleDepts = sysRoleDepts;
        this.sysRoles = sysRoles;


    }

    public Long getParentId(SysDept node) {
        return node.getParentId();
    }

    ;

    public void setItem(DeptManger node, List<DeptManger> childList) {
        node.setChildren(childList);
    }

    private List<SysRole> getIncludeSysRoleByDeptId(Long deptId) {
        List<SysRoleDept> collect = sysRoleDepts.stream().filter(sysRoleDept -> sysRoleDept.getDeptId().equals(deptId)).toList();
        return sysRoles.stream().filter(sysRole -> collect.stream().anyMatch(sysRoleDept -> sysRoleDept.getRoleId().equals(sysRole.getId()))).collect(Collectors.toList());
    }

    public List<DeptManger> buildHierarchy() {
        List<DeptManger> topLevelNodes = new ArrayList<>();

        for (SysDept node : nodeList) {
            Long parentId = getParentId(node);
            List<SysRole> includeSysRoleByDeptId = getIncludeSysRoleByDeptId(node.getId());
            if (parentId.equals(0L)) {
                DeptManger deptManger = new DeptManger();
                BeanUtils.copyProperties(node, deptManger);
                deptManger.setChildren(new ArrayList<>());
                deptManger.setRoles(includeSysRoleByDeptId);
                topLevelNodes.add(deptManger);
            } else {
                List<DeptManger> childList = childMap.getOrDefault(parentId, new ArrayList<>());
                DeptManger deptManger = new DeptManger();
                BeanUtils.copyProperties(node, deptManger);
                deptManger.setChildren(new ArrayList<>());
                deptManger.setRoles(includeSysRoleByDeptId);
                childList.add(deptManger);
                childMap.put(parentId, childList);
            }
        }

        for (DeptManger node : topLevelNodes) {
            buildChildHierarchy(node);
        }

        return topLevelNodes;
    }

    private void buildChildHierarchy(DeptManger node) {
        List<DeptManger> childList = childMap.get(node.getId());
        if (childList != null) {
            for (DeptManger childNode : childList) {
                buildChildHierarchy(childNode);
            }
            setItem(node, childList);
        }
    }
}
