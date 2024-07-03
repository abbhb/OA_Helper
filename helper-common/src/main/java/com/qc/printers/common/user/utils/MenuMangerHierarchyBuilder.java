package com.qc.printers.common.user.utils;

import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.common.user.domain.dto.MenuManger;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuMangerHierarchyBuilder {
    private List<SysMenu> nodeList;
    private Map<Long, List<MenuManger>> childMap;

    public MenuMangerHierarchyBuilder(List<SysMenu> nodeList) {
        this.nodeList = nodeList;
        this.childMap = new HashMap<>();
    }

    public Long getParentId(SysMenu node) {
        return node.getParentId();
    }

    ;


    public void setItem(MenuManger node, List<MenuManger> childList) {
        node.setChildren(childList);
    }

    ;


    public List<MenuManger> buildHierarchy() {
        List<MenuManger> topLevelNodes = new ArrayList<>();

        for (SysMenu node : nodeList) {
            Long parentId = getParentId(node);
            if (parentId.equals(0L)) {
                MenuManger menuManger = new MenuManger();
                BeanUtils.copyProperties(node, menuManger);
                menuManger.setChildren(new ArrayList<>());
                topLevelNodes.add(menuManger);
            } else {
                List<MenuManger> childList = childMap.getOrDefault(parentId, new ArrayList<>());
                MenuManger menuManger = new MenuManger();
                BeanUtils.copyProperties(node, menuManger);
                menuManger.setChildren(new ArrayList<>());
                childList.add(menuManger);
                childMap.put(parentId, childList);
            }
        }

        for (MenuManger node : topLevelNodes) {
            buildChildHierarchy(node);
        }

        return topLevelNodes;
    }

    private void buildChildHierarchy(MenuManger node) {

        List<MenuManger> childList = childMap.get(node.getId());
        if (childList != null) {
            for (MenuManger childNode : childList) {
                buildChildHierarchy(childNode);
            }
            setItem(node, childList);
        }
    }
}
