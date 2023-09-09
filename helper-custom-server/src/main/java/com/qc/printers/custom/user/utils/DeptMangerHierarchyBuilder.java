package com.qc.printers.custom.user.utils;

import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.custom.user.domain.vo.response.dept.DeptManger;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeptMangerHierarchyBuilder {
    private List<SysDept> nodeList;
    private Map<Long, List<DeptManger>> childMap;

    public DeptMangerHierarchyBuilder(List<SysDept> nodeList) {
        this.nodeList = nodeList;
        this.childMap = new HashMap<>();
    }

    public Long getParentId(SysDept node) {
        return node.getParentId();
    }

    ;

    public void setItem(DeptManger node, List<DeptManger> childList) {
        node.setChildren(childList);
    }

    ;


    public List<DeptManger> buildHierarchy() {
        List<DeptManger> topLevelNodes = new ArrayList<>();

        for (SysDept node : nodeList) {
            Long parentId = getParentId(node);
            if (parentId.equals(0L)) {
                DeptManger deptManger = new DeptManger();
                BeanUtils.copyProperties(node, deptManger);
                deptManger.setChildren(new ArrayList<>());
                topLevelNodes.add(deptManger);
            } else {
                List<DeptManger> childList = childMap.getOrDefault(parentId, new ArrayList<>());
                DeptManger deptManger = new DeptManger();
                BeanUtils.copyProperties(node, deptManger);
                deptManger.setChildren(new ArrayList<>());
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
