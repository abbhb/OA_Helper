package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.signin.domain.entity.SigninBc;
import com.qc.printers.common.signin.service.SigninBcService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signin_bc")
@Api("班次相关的接口")
@CrossOrigin("*")
@Slf4j
public class SigninBcController {
    @Autowired
    private SigninBcService signinBcService;

    @PostMapping("/add")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "添加班次", notes = "")
    public R<String> add(@RequestBody SigninBc signinBc) {
        log.info("添加班次");
        log.info("signinBc={}", signinBc);
        return R.success(signinBcService.addSigninBc(signinBc));
    }

    @DeleteMapping("/delete")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "删除班次", notes = "")
    public R<String> delete(String id) {
        log.info("删除班次");
        return R.success(signinBcService.deleteSigninBc(id));
    }

    @PutMapping("/update")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "更新班次", notes = "")
    public R<String> update(@RequestBody SigninBc signinBc) {
        log.info("更新班次");
        return R.success(signinBcService.updateSigninBc(signinBc));
    }


    @GetMapping("/list")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "查询班次", notes = "")
    public R<List<SigninBc>> list() {
        log.info("查询班次");
        List<SigninBc> signinBcs = signinBcService.listSigninBc();
        log.info("{}", signinBcs);
        return R.success(signinBcs);
    }

}
