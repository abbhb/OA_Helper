package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.common.user.service.ISysMenuService;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuManger;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuResult;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuResultNode;
import com.qc.printers.custom.user.domain.vo.response.menu.MetaNode;
import com.qc.printers.custom.user.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private UserDao userDao;


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
            metaNode.setFrame(sysMenu.getIsFrame().equals(1));
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
                metaNode2.setFrame(sysMenu2.getIsFrame().equals(1));
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
                menuManger.setCreateUserName(userDao.getById(sysMenu.getCreateUser()).getName());
            }
            if (sysMenu.getUpdateUser() != null) {
                if (sysMenu.getUpdateUser().equals(sysMenu.getCreateUser())) {
                    menuManger.setUpdateUserName(menuManger.getCreateUserName());
                } else {
                    menuManger.setUpdateUserName(userDao.getById(sysMenu.getUpdateUser()).getName());
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
                    menuManger2.setCreateUserName(userDao.getById(sysMenu2.getCreateUser()).getName());
                }
                if (sysMenu.getUpdateUser() != null) {
                    if (sysMenu.getUpdateUser().equals(sysMenu2.getCreateUser())) {
                        menuManger2.setUpdateUserName(menuManger2.getCreateUserName());
                    } else {
                        menuManger2.setUpdateUserName(userDao.getById(sysMenu2.getUpdateUser()).getName());
                    }
                }
                menuManger2.setUpdateTime(sysMenu2.getUpdateTime());
                menuManger2.setParentId(sysMenu2.getParentId());
                menuManger2.setUpdateUser(sysMenu2.getUpdateUser());
                //第三层--叶子
                List<MenuManger> collect3 = list.stream().filter(sysMenu3 -> sysMenu3.getParentId().equals(menuManger2.getId())).map(newsysMeny3 -> new MenuManger(newsysMeny3.getId(), newsysMeny3.getName(), newsysMeny3.getLocale(), newsysMeny3.getParentId(), newsysMeny3.getOrderNum(), newsysMeny3.getPath(), newsysMeny3.getIsFrame(), newsysMeny3.getIsCache(), newsysMeny3.getType(), newsysMeny3.getIsShow(), newsysMeny3.getStatus(), newsysMeny3.getPerms(), newsysMeny3.getIcon(), newsysMeny3.getCreateUser(), userDao.getById(newsysMeny3.getCreateUser()).getName(), newsysMeny3.getCreateTime(), newsysMeny3.getUpdateUser(), userDao.getById(newsysMeny3.getUpdateUser()).getName(), newsysMeny3.getUpdateTime(), null)).collect(Collectors.toList());
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

    private void checkParamsBase(MenuManger menuManger) {
        if (StringUtils.isEmpty(menuManger.getType())) {
            throw new IllegalArgumentException("参数异常");
        }
        if (menuManger.getIsFrame() == null) {
            menuManger.setIsFrame(0);
        }
        if ((!menuManger.getIsFrame().equals(1)) && (!menuManger.getIsFrame().equals(0))) {
            menuManger.setIsFrame(0);
        }
        if (menuManger.getIsCache() == null) {
            menuManger.setIsCache(0);
        }
        if ((!menuManger.getIsCache().equals(1)) && (!menuManger.getIsCache().equals(0))) {
            menuManger.setIsCache(0);
        }
        if (menuManger.getIsShow() == null) {
            menuManger.setIsShow(0);
        }
        if ((!menuManger.getIsShow().equals(1)) && (!menuManger.getIsShow().equals(0))) {
            menuManger.setIsShow(0);
        }
        if (menuManger.getStatus() == null) {
            menuManger.setStatus(1);
        }
        if ((!menuManger.getStatus().equals(1)) && (!menuManger.getStatus().equals(0))) {
            menuManger.setStatus(1);
        }
        if (menuManger.getType().equals("M") || menuManger.getType().equals("C")) {
            if (!menuManger.getIsFrame().equals(1)) {
                if (StringUtils.isEmpty(menuManger.getPath())) {
                    throw new CustomException("请输入路由Path");
                }
                if (StringUtils.isEmpty(menuManger.getName())) {
                    throw new CustomException("请输入路由名称");
                }
            }
            if (StringUtils.isEmpty(menuManger.getLocale())) {
                throw new CustomException("请输入Locale");
            }
        }
        if (menuManger.getType().equals("F")) {
            if (StringUtils.isEmpty(menuManger.getLocale())) {
                throw new CustomException("参数异常");
            }
        }
        if (menuManger.getParentId() == null) {
            menuManger.setParentId(0L);
        }
        if (menuManger.getSort() <= 0 || menuManger.getSort() > 1000) {
            throw new CustomException("Sort参数不合理");
        }

    }

    private void checkParamsAdd(MenuManger menuManger) {
        checkParamsBase(menuManger);
    }

    private void checkParamsUpdate(MenuManger menuManger) {
        checkParamsBase(menuManger);
        if (menuManger.getId() == null) {
            throw new CustomException("id null");
        }
    }

    @Transactional
    @Override
    public String addMenu(MenuManger menuManger) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("未鉴权");
        }
        // 如果当前的上级已经是第三级禁止添加
        Integer hierarchicalSeries = iSysMenuService.getHierarchicalSeries(menuManger.getParentId());
        if (hierarchicalSeries >= 3) {
            throw new CustomException("禁止层级>3");
        }
        checkParamsAdd(menuManger);
        SysMenu sysMenu = new SysMenu();
        sysMenu.setId(null);
        sysMenu.setIsFrame(menuManger.getIsFrame());
        sysMenu.setIcon(menuManger.getIcon());
        sysMenu.setLocale(menuManger.getLocale());
        sysMenu.setName(menuManger.getName());
        sysMenu.setPath(menuManger.getPath());
        sysMenu.setPerms(menuManger.getPerms());
        sysMenu.setType(menuManger.getType());
        sysMenu.setIsCache(menuManger.getIsCache());
        sysMenu.setIsShow(menuManger.getIsShow());
        sysMenu.setStatus(menuManger.getStatus());
        sysMenu.setOrderNum(menuManger.getSort());
        sysMenu.setParentId(menuManger.getParentId());
        boolean save = iSysMenuService.save(sysMenu);
        if (save) {
            return "添加菜单成功";
        }
        return "添加菜单失败";
    }


    @Transactional
    @Override
    public String updateMenu(MenuManger menuManger) {
        checkParamsUpdate(menuManger);
        // 如果当前的上级已经是第三级禁止更新
        Integer hierarchicalSeries = iSysMenuService.getHierarchicalSeries(menuManger.getParentId());
        if (hierarchicalSeries >= 3) {
            throw new CustomException("禁止层级>3");
        }
        LambdaUpdateWrapper<SysMenu> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(SysMenu::getId, menuManger.getId());
        lambdaUpdateWrapper.set(SysMenu::getIcon, menuManger.getIcon());
        lambdaUpdateWrapper.set(SysMenu::getIsCache, menuManger.getIsCache());
        lambdaUpdateWrapper.set(SysMenu::getIsFrame, menuManger.getIsFrame());
        lambdaUpdateWrapper.set(SysMenu::getIsShow, menuManger.getIsShow());
        lambdaUpdateWrapper.set(SysMenu::getParentId, menuManger.getParentId());
        lambdaUpdateWrapper.set(SysMenu::getPerms, menuManger.getPerms());
        lambdaUpdateWrapper.set(SysMenu::getType, menuManger.getType());
        lambdaUpdateWrapper.set(SysMenu::getPath, menuManger.getPath());
        lambdaUpdateWrapper.set(SysMenu::getStatus, menuManger.getStatus());
        lambdaUpdateWrapper.set(SysMenu::getLocale, menuManger.getLocale());
        lambdaUpdateWrapper.set(SysMenu::getName, menuManger.getName());
        lambdaUpdateWrapper.set(SysMenu::getOrderNum, menuManger.getSort());
        boolean update = iSysMenuService.update(lambdaUpdateWrapper);
        if (update) {
            return "更新菜单成功";
        }
        return "更新菜单失败";
    }


    @Transactional
    @Override
    public String deleteMenu(String id) {
        // todo: 此处有重大bug，子菜单级联删除，子菜单的子菜单也得如此
        if (StringUtils.isEmpty(id)) {
            throw new CustomException("id不能为空");
        }
        if (id.contains(",")) {
            throw new CustomException("为了安全菜单禁止多选删除！");
        }
        LambdaQueryWrapper<SysMenu> sysMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysMenuLambdaQueryWrapper.eq(SysMenu::getParentId, Long.valueOf(id));
        List<SysMenu> children = iSysMenuService.list(sysMenuLambdaQueryWrapper);
        if (children == null || children.size() == 0) {
            iSysMenuService.removeById(Long.valueOf(id));
            return "删除成功";
        }
        List<Long> needDeleteId = new ArrayList<>();
        //递归删除
        List<SysMenu> last = new ArrayList<>();
        last.addAll(children);
        while (last != null) {
            List<SysMenu> lastCopy = new ArrayList<>(last);
            last = new ArrayList<>();
            for (SysMenu s :
                    lastCopy) {
                needDeleteId.add(s.getId());
                LambdaQueryWrapper<SysMenu> sysMenuLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                sysMenuLambdaQueryWrapper2.eq(SysMenu::getParentId, s.getId());
                last.addAll(iSysMenuService.list(sysMenuLambdaQueryWrapper2));
            }
        }
        iSysMenuService.removeByIds(needDeleteId);
        return "删除成功";
    }


}
