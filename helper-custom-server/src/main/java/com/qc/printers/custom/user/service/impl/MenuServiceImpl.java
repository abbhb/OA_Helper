package com.qc.printers.custom.user.service.impl;

import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.custom.user.domain.vo.response.MenuResult;
import com.qc.printers.custom.user.domain.vo.response.MenuResultNode;
import com.qc.printers.custom.user.domain.vo.response.MetaNode;
import com.qc.printers.custom.user.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    @Override
    public List<MenuResult> getUserMenu(UserInfo userInfo) {
        Set<SysMenu> sysMenus = userInfo.getSysMenus();
        log.info("sysMenus = {}", sysMenus);
        Set<SysMenu> sysMenuSet = sysMenus.stream().filter(sysMenu -> (sysMenu.getParentId() == null || sysMenu.getParentId().equals(0L))).collect(Collectors.toSet());
        List<MenuResult> menuResultList = new ArrayList<>();
        for (SysMenu sysMenu :
                sysMenuSet) {
            MetaNode metaNode = new MetaNode();
            metaNode.setIcon(sysMenu.getIcon());
            metaNode.setOrder(sysMenu.getOrderNum());
            metaNode.setLocale(sysMenu.getLocale());
            MenuResult menuResult = new MenuResult();
            List<MenuResultNode> menuResultNodes = new ArrayList<>();
            menuResult.setName(sysMenu.getName());
            menuResult.setPath(sysMenu.getPath());
            menuResult.setMeta(metaNode);
            for (SysMenu sysMenu2 :
                    sysMenus.stream().filter(sysMenu1 -> (sysMenu1.getParentId() != null) && (sysMenu1.getParentId().equals(sysMenu.getId()))).collect(Collectors.toSet())) {
                MenuResultNode menuResultNode = new MenuResultNode();
                MetaNode metaNode2 = new MetaNode();
                metaNode2.setLocale(sysMenu2.getLocale());
                metaNode2.setIcon(sysMenu2.getIcon());
                metaNode2.setOrder(sysMenu2.getOrderNum());
                menuResultNode.setName(sysMenu2.getName());
                menuResultNode.setPath(sysMenu2.getPath());
                menuResultNode.setMeta(metaNode2);
                menuResultNodes.add(menuResultNode);
            }
            menuResultNodes.sort((m1, m2) -> {
                Integer order1 = m1.getMeta().getOrder();
                Integer order2 = m2.getMeta().getOrder();
                return order1.compareTo(order2);
            });
            menuResult.setChildren(menuResultNodes);
//            if (menuResultNodes.size()==0){
//                MenuResult menuResult1 = new MenuResult();
//                menuResult1.set
//                menuResultList.add(sysMenu);
//                continue;
//            }
            menuResultList.add(menuResult);
        }
        menuResultList.sort((m1, m2) -> {
            Integer order1 = m1.getMeta().getOrder();
            Integer order2 = m2.getMeta().getOrder();
            return order1.compareTo(order2);
        });
        return menuResultList;
    }
}
