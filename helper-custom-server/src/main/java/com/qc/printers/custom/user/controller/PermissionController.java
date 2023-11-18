package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysMenu;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RestController
@RequestMapping("/permission")
@Api("和权限相关的接口")
@CrossOrigin("*")
@Slf4j
public class PermissionController {
    @NeedToken
    @GetMapping("/check")
    @ApiOperation(value = "校验权限是否拥有")
    public R<Integer> checkPermission(@RequestParam(name = "permission") String permission) {
        log.info("校验权限是否拥有");
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("没有权限");
        }
        Set<SysMenu> sysMenus = currentUser.getSysMenus();
        // 高效查找 needPermission 是否存在于 sysMenus 中的某个对象的某个值中
        boolean exists = sysMenus.stream()
                .filter(sysMenu -> StringUtils.isNotEmpty(sysMenu.getPerms()))
                .map(SysMenu::getPerms)
                .anyMatch(s -> s.equals(permission));
        return R.success(exists ? 1 : 0);
    }
}
