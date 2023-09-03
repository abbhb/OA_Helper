package com.qc.printers.custom.user.service.impl;

import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.common.user.service.ISysMenuService;
import com.qc.printers.common.user.service.IUserService;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuManger;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuResult;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuResultNode;
import com.qc.printers.custom.user.domain.vo.response.menu.MetaNode;
import com.qc.printers.custom.user.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    @Autowired
    private ISysMenuService iSysMenuService;

    @Autowired
    private IUserService iUserService;


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
            metaNode.setShow(sysMenu.getIsShow().equals(1));
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
                metaNode2.setShow(sysMenu2.getIsShow().equals(1));
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

    @Override
    public List<MenuManger> getMenuList() {
        List<SysMenu> list = iSysMenuService.list();
        List<MenuManger> menuMangerList = new ArrayList<>();
        Set<SysMenu> sysMenuSet = list.stream().filter(sysMenu -> sysMenu.getParentId().equals(0L)).collect(Collectors.toSet());
        for (SysMenu sysMenu :
                sysMenuSet) {
            MenuManger menuManger = new MenuManger();
            menuManger.setId(sysMenu.getId());
            menuManger.setIcon(sysMenu.getIcon());
            menuManger.setName(sysMenu.getName());
            menuManger.setPath(sysMenu.getPath());
            menuManger.setLocale(sysMenu.getLocale());
            menuManger.setCreateTime(sysMenu.getCreateTime());
            menuManger.setCreateUser(sysMenu.getCreateUser());
            menuManger.setIsFrame(sysMenu.getIsFrame());
            menuManger.setIsShow(sysMenu.getIsShow());
            menuManger.setIsCache(sysMenu.getIsCache());
            menuManger.setSort(sysMenu.getOrderNum());
            menuManger.setType(sysMenu.getType());
            menuManger.setPerms(sysMenu.getPerms());
            menuManger.setStatus(sysMenu.getStatus());
            menuManger.setCreateUserName("信息缺失");
            menuManger.setUpdateUserName("信息缺失");
            if (sysMenu.getCreateUser() != null) {
                menuManger.setCreateUserName(iUserService.getById(sysMenu.getCreateUser()).getName());
            }
            if (sysMenu.getUpdateUser() != null) {
                if (sysMenu.getUpdateUser().equals(sysMenu.getCreateUser())) {
                    menuManger.setUpdateUserName(menuManger.getCreateUserName());
                } else {
                    menuManger.setUpdateUserName(iUserService.getById(sysMenu.getUpdateUser()).getName());
                }
            }
            menuManger.setUpdateTime(sysMenu.getUpdateTime());
            menuManger.setParentId(sysMenu.getParentId());
            menuManger.setUpdateUser(sysMenu.getUpdateUser());

            //子节点逻辑--第二层：
            Set<SysMenu> sysMenuSet2 = list.stream().filter(sysMenu2 -> sysMenu2.getParentId().equals(menuManger.getId())).collect(Collectors.toSet());
            List<MenuManger> menuMangerList2 = new ArrayList<>();
            for (SysMenu sysMenu2 :
                    sysMenuSet2) {
                MenuManger menuManger2 = new MenuManger();
                menuManger2.setId(sysMenu2.getId());
                menuManger2.setIcon(sysMenu2.getIcon());
                menuManger2.setName(sysMenu2.getName());
                menuManger2.setPath(sysMenu2.getPath());
                menuManger2.setLocale(sysMenu2.getLocale());
                menuManger2.setCreateTime(sysMenu2.getCreateTime());
                menuManger2.setCreateUser(sysMenu2.getCreateUser());
                menuManger2.setIsFrame(sysMenu2.getIsFrame());
                menuManger2.setIsShow(sysMenu2.getIsShow());
                menuManger2.setIsCache(sysMenu2.getIsCache());
                menuManger2.setSort(sysMenu2.getOrderNum());
                menuManger2.setType(sysMenu2.getType());
                menuManger2.setPerms(sysMenu2.getPerms());
                menuManger2.setStatus(sysMenu2.getStatus());
                menuManger2.setCreateUserName("信息缺失");
                menuManger2.setUpdateUserName("信息缺失");
                if (sysMenu.getCreateUser() != null) {
                    menuManger2.setCreateUserName(iUserService.getById(sysMenu2.getCreateUser()).getName());
                }
                if (sysMenu.getUpdateUser() != null) {
                    if (sysMenu.getUpdateUser().equals(sysMenu2.getCreateUser())) {
                        menuManger2.setUpdateUserName(menuManger2.getCreateUserName());
                    } else {
                        menuManger2.setUpdateUserName(iUserService.getById(sysMenu2.getUpdateUser()).getName());
                    }
                }
                menuManger2.setUpdateTime(sysMenu2.getUpdateTime());
                menuManger2.setParentId(sysMenu2.getParentId());
                menuManger2.setUpdateUser(sysMenu2.getUpdateUser());
                //第三层--叶子
                List<MenuManger> collect3 = list.stream().filter(sysMenu3 -> sysMenu3.getParentId().equals(menuManger2.getId())).map(newsysMeny3 -> new MenuManger(newsysMeny3.getId(), newsysMeny3.getName(), newsysMeny3.getLocale(), newsysMeny3.getParentId(), newsysMeny3.getOrderNum(), newsysMeny3.getPath(), newsysMeny3.getIsFrame(), newsysMeny3.getIsCache(), newsysMeny3.getType(), newsysMeny3.getIsShow(), newsysMeny3.getStatus(), newsysMeny3.getPerms(), newsysMeny3.getIcon(), newsysMeny3.getCreateUser(), iUserService.getById(newsysMeny3.getCreateUser()).getName(), newsysMeny3.getCreateTime(), newsysMeny3.getUpdateUser(), iUserService.getById(newsysMeny3.getUpdateUser()).getName(), newsysMeny3.getUpdateTime(), null)).collect(Collectors.toList());
                collect3.sort(Comparator.comparing(MenuManger::getSort));
                menuManger2.setChildren(collect3);
                menuMangerList2.add(menuManger2);
            }
            menuMangerList2.sort(Comparator.comparing(MenuManger::getSort));
            menuManger.setChildren(menuMangerList2);
            menuMangerList.add(menuManger);
            log.info("menuManger{}", menuManger);

        }
        menuMangerList.sort(Comparator.comparing(MenuManger::getSort));
        return menuMangerList;
    }
}
