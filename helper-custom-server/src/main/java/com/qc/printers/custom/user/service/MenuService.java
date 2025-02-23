package com.qc.printers.custom.user.service;

import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.dto.MenuManger;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuResult;

import java.util.List;

public interface MenuService {
    List<MenuResult> getUserMenu(UserInfo userInfo);

    @Deprecated
    List<MenuManger> getMenuList();
    List<MenuManger> getMenuTree();

    String addMenu(MenuManger menuManger);

    String updateMenu(MenuManger menuManger);

    String deleteMenu(String id);
}
