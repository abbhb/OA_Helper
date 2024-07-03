package com.qc.printers.common.user.domain.dto;

import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.SysRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeptManger extends SysDept implements Serializable {
    private String leader;


    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
    private List<SysRole> roles;

    private List<DeptManger> children;

    public static DeptManger findDeptManagerById(List<DeptManger> deptList, Long id) {
        for (DeptManger deptManager : deptList) {
            if (deptManager.getId().equals(id) ) {
                return deptManager;
            }
            // 如果部门经理有子部门，递归地搜索子部门列表
            if (deptManager.getChildren() != null && !deptManager.getChildren().isEmpty()) {
                DeptManger foundInSubList = findDeptManagerById(deptManager.getChildren(), id);
                if (foundInSubList != null) {
                    return foundInSubList;
                }
            }
        }
        return null; // 如果没有找到，返回null
    }
    private static void addDescendantIds(List<DeptManger> children, List<Long> ids) {
        if (children != null) {
            for (DeptManger child : children) {
                ids.add(child.getId());
                // 递归地添加子部门的子部门 id
                addDescendantIds(child.getChildren(), ids);
            }
        }
    }
    public static List<Long> getIdsWithDescendants(DeptManger deptManager) {
        List<Long> ids = new ArrayList<>();
        if (deptManager != null) {
            // 添加当前部门的 id
            ids.add(deptManager.getId());
            // 递归地添加所有子部门的 id
            addDescendantIds(deptManager.getChildren(), ids);
        }
        return ids;
    }


    public static void sortRecursion(List<DeptManger> deptMangers) {
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


}
