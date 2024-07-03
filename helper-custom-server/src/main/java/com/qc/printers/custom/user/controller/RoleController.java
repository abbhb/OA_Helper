package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.enums.DataScopeEnum;
import com.qc.printers.custom.user.domain.vo.response.DataScopeResp;
import com.qc.printers.custom.user.domain.vo.response.role.RoleManger;
import com.qc.printers.custom.user.domain.vo.response.role.RoleMangerRoot;
import com.qc.printers.custom.user.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/role")
@Api("和角色相关的接口")
@CrossOrigin("*")
@Slf4j
public class RoleController {
    @Autowired
    private RoleService roleService;


    @PostMapping("/list")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:role:list")
    @NeedToken
    @ApiOperation(value = "获取全部角色", notes = "")
    public R<RoleMangerRoot> list() {
        log.info("获取角色");
        RoleMangerRoot roleManger = roleService.getRoleList();
        log.info("roleManger={}", roleManger);
        return R.success(roleManger);
    }

    @NeedToken
    @GetMapping("/list-tag")
    @ApiOperation(value = "获取全部角色用作选择器", notes = "不携带菜单")
    public R<List<SysRole>> listTag() {
        log.info("获取角色");
        List<SysRole> sysRoles = roleService.list();
        log.info("sysRoles={}", sysRoles);
        return R.success(sysRoles);
    }


    @NeedToken
    @GetMapping("/data-scopes")
    @ApiOperation(value = "获取全部角色用作选择器", notes = "不携带菜单")
    public R<List<DataScopeResp>> listDataScopes() {
        return R.success(Arrays.stream(DataScopeEnum.values()).map(scope -> new DataScopeResp(scope.getType(), scope.getName(), scope.getDesc()))
                .collect(Collectors.toList()));
    }

    @PostMapping("/add")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:role:add")
    @NeedToken
    @ApiOperation(value = "添加角色", notes = "")
    public R<String> add(@RequestBody RoleManger roleManger) {
        log.info("添加角色");
        log.info("roleManger={}", roleManger);
        return R.successOnlyObject(roleService.addRole(roleManger));
    }

    @PutMapping("/update")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:role:update")
    @NeedToken
    @ApiOperation(value = "update角色", notes = "")
    public R<String> update(@RequestBody RoleManger roleManger) {
        log.info("update角色");
        log.info("roleManger={}", roleManger);
        return R.successOnlyObject(roleService.updateRole(roleManger));
    }

    @DeleteMapping("/delete")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:role:delete")
    @NeedToken
    @ApiOperation(value = "删除角色", notes = "")
    public R<String> delete(String id) {
        log.info("删除角色.{}", id);
        if (StringUtils.isEmpty(id)) {
            return R.error("参数不全");
        }
        return R.successOnlyObject(roleService.deleteRole(id));

    }

}
