package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.signin.domain.dto.SigninGroupDto;
import com.qc.printers.common.signin.service.SigninGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@ResponseBody+@Controller
@RestController
@RequestMapping("/signin_group")
@Api("考勤组相关的接口")
@CrossOrigin("*")
@Slf4j
public class SigninGroupController {

    @Autowired
    private SigninGroupService signinGroupService;

    @PostMapping("/add")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "添加考勤组", notes = "")
    public R<String> add(@RequestBody SigninGroupDto signinGroupDto) {
        log.info("添加考勤组");
        log.info("signinGroupDto={}", signinGroupDto);
        return R.success(signinGroupService.addSigninGroup(signinGroupDto));
    }


    @DeleteMapping("/delete")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "删除考勤组", notes = "")
    public R<String> delete(String id) {
        log.info("删除考勤组");
        return R.success(signinGroupService.deleteSigninGroup(id));
    }

    @PutMapping("/update")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "更新考勤组规则信息", notes = "")
    public R<String> update(@RequestBody SigninGroupDto signinGroupDto) {
        log.info("更新考勤组");
        return R.success(signinGroupService.updateSigninGroup(signinGroupDto));
    }

    @GetMapping("/list")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "获取考勤组规则信息", notes = "")
    public R<List<SigninGroupDto>> list() {
        log.info("获取考勤组规则信息");
        return R.success(signinGroupService.listSigninGroup());
    }
}
