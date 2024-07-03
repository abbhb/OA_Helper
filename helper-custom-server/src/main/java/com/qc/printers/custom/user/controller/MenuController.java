package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.dto.MenuManger;
import com.qc.printers.custom.user.domain.vo.response.menu.MenuResult;
import com.qc.printers.custom.user.service.MenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController//@ResponseBody+@Controller
@RequestMapping("/menu")
@Api("和菜单相关的接口")
@CrossOrigin("*")
@Slf4j
public class MenuController {
    @Autowired
    private MenuService menuService;

    @PostMapping("/get")
    @NeedToken
    @ApiOperation(value = "获取用户获取菜单", notes = "")
    public R<List<MenuResult>> get() {
        log.info("获取菜单");
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        List<MenuResult> userMenu = menuService.getUserMenu(currentUser);
        log.info("userMenu={}", userMenu);
        return R.success(userMenu);
    }

    @PostMapping("/list")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:menu:list")
    @NeedToken
    @ApiOperation(value = "获取全部菜单", notes = "")
    public R<List<MenuManger>> list() {
        log.info("获取菜单");
        List<MenuManger> userMenu = menuService.getMenuList();
        log.info("userMenu={}", userMenu);
        return R.success(userMenu);
    }

    @PostMapping("/add")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:menu:add")
    @NeedToken
    @ApiOperation(value = "添加菜单", notes = "")
    public R<String> add(@RequestBody MenuManger menuManger) {
        log.info("添加菜单");
        log.info("menuManger={}", menuManger);
        return R.successOnlyObject(menuService.addMenu(menuManger));
    }

    @PutMapping("/update")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:menu:update")
    @NeedToken
    @ApiOperation(value = "update菜单", notes = "")
    public R<String> update(@RequestBody MenuManger menuManger) {
        log.info("update菜单");
        log.info("menuManger={}", menuManger);
        return R.successOnlyObject(menuService.updateMenu(menuManger));
    }

    @DeleteMapping("/delete")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:menu:delete")
    @NeedToken
    @ApiOperation(value = "删除菜单", notes = "")
    public R<String> delete(String id) {
        log.info("删除菜单.{}", id);
//        List<MenuManger> userMenu = menuService.getMenuList();
//        return R.successOnlyObject(userMenu);

        if (StringUtils.isEmpty(id)) {
            return R.error("参数不全");
        }
        return R.successOnlyObject(menuService.deleteMenu(id));

    }

}
