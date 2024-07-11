package com.qc.printers.common.user.utils;

import com.qc.printers.common.activiti.utils.SpringUtils;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.*;
import com.qc.printers.common.user.domain.dto.DeptManger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DeptMangerHierarchyBuilder {
    private List<SysDept> nodeList;
    private Map<Long, List<DeptManger>> childMap;

    private Set<SysRoleDept> sysRoleDepts;
    private Set<SysRole> sysRoles;
    private Set<SysDeptLeaderRole> sysDeptLeaderRoles;

    private int type = 1;

    public DeptMangerHierarchyBuilder(List<SysDept> nodeList, Set<SysRole> sysRoles, Set<SysRoleDept> sysRoleDepts,Set<SysDeptLeaderRole> sysDeptLeaderRoles) {
        this.nodeList = nodeList;
        this.childMap = new HashMap<>();
        this.sysRoleDepts = sysRoleDepts;
        this.sysRoles = sysRoles;
        this.sysDeptLeaderRoles = sysDeptLeaderRoles;
    }

    /**
     * @param nodeList
     * @param sysRoleDepts
     * @param sysRoles
     * @param type         0为仅需要部门数据，无需应用数据
     */
    public DeptMangerHierarchyBuilder(List<SysDept> nodeList, Set<SysRoleDept> sysRoleDepts, Set<SysRole> sysRoles, int type) {
        this.nodeList = nodeList;
        this.sysRoleDepts = sysRoleDepts;
        this.childMap = new HashMap<>();
        this.sysRoles = sysRoles;
        this.type = type;
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
    private List<SysRole> getIncludeLeaderSysRoleByDeptId(Long deptId) {
        List<SysDeptLeaderRole> collect = sysDeptLeaderRoles.stream().filter(sysDeptLeaderRole -> sysDeptLeaderRole.getDeptId().equals(deptId)).toList();
        return sysRoles.stream().filter(sysRole -> collect.stream().anyMatch(sysDeptLeaderRole -> sysDeptLeaderRole.getRoleId().equals(sysRole.getId()))).collect(Collectors.toList());
    }


    public List<DeptManger> buildHierarchy() {
        UserDao userDao = SpringUtils.getBean(UserDao.class);
        List<DeptManger> topLevelNodes = new ArrayList<>();
        log.info("{}", nodeList);
        for (SysDept node : nodeList) {
            Long parentId = getParentId(node);
            List<SysRole> includeSysRoleByDeptId = null;
            List<SysRole> includeLeaderSysRoleByDeptId = null;
            if (type == 1) {
                includeSysRoleByDeptId = getIncludeSysRoleByDeptId(node.getId());
                includeLeaderSysRoleByDeptId = getIncludeLeaderSysRoleByDeptId(node.getId());
            }
            if (parentId.equals(0L)) {
                DeptManger deptManger = new DeptManger();
                BeanUtils.copyProperties(node, deptManger);
                if (node.getLeaderId() != null) {
                    User byId = userDao.getById(node.getLeaderId());
                    if (byId != null) {
                        deptManger.setLeaderId(node.getLeaderId());
                        deptManger.setLeader(byId.getName());
                        deptManger.setEmail(byId.getEmail());
                        deptManger.setPhone(byId.getPhone());

                    }
                }

                deptManger.setChildren(new ArrayList<>());
                if (type == 1) {
                    deptManger.setRoles(includeSysRoleByDeptId);
                    deptManger.setLeaderRoles(includeLeaderSysRoleByDeptId);
                }
                topLevelNodes.add(deptManger);
            } else {
                List<DeptManger> childList = childMap.getOrDefault(parentId, new ArrayList<>());
                DeptManger deptManger = new DeptManger();
                BeanUtils.copyProperties(node, deptManger);
                if (node.getLeaderId() != null) {
                    User byId = userDao.getById(node.getLeaderId());
                    if (byId != null) {
                        deptManger.setLeaderId(node.getLeaderId());
                        deptManger.setLeader(byId.getName());
                        deptManger.setEmail(byId.getEmail());
                        deptManger.setPhone(byId.getPhone());

                    }
                }
                deptManger.setChildren(new ArrayList<>());
                if (type == 1) {
                    deptManger.setRoles(includeSysRoleByDeptId);
                    deptManger.setLeaderRoles(includeLeaderSysRoleByDeptId);
                }
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
