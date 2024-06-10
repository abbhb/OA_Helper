package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;
import com.qc.printers.common.signin.service.SigninDeviceMangerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signin_device")
@Api("设备相关的接口")
@CrossOrigin("*")
@Slf4j
public class SigninDeviceController {

    @Autowired
    private SigninDeviceMangerService signinDeviceMangerService;

    @GetMapping("/list")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-device:list")
    @NeedToken
    @ApiOperation(value = "查询已经绑定的设备", notes = "")
    public R<List<SigninDeviceDto>> list() {
        log.info("查询已经绑定的设备");
        List<SigninDeviceDto> bindDeviceList = signinDeviceMangerService.getBindDeviceList();
        log.info("{}", bindDeviceList);
        return R.success(bindDeviceList);
    }

    @GetMapping("/list-online")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-device:list")
    @NeedToken
    @ApiOperation(value = "查询在线设备", notes = "")
    public R<List<SigninDeviceDto>> listOnline() {
        log.info("查询在线设备");
        List<SigninDeviceDto> bindDeviceList = signinDeviceMangerService.getCanBindDeviceList();
        log.info("{}", bindDeviceList);

        return R.success(bindDeviceList);
    }


    @PostMapping("/add")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-device:add")
    @NeedToken
    @ApiOperation(value = "添加绑定设备", notes = "")
    public R<String> addDevice(@RequestBody SigninDeviceDto signinDeviceDto) {
        log.info("添加绑定设备");
        return R.successOnlyObject(signinDeviceMangerService.addBindDevice(signinDeviceDto));
    }

    @PutMapping("/update")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-device:update")
    @NeedToken
    @ApiOperation(value = "更新绑定设备", notes = "")
    public R<String> updateDevice(@RequestBody SigninDeviceDto signinDeviceDto) {
        log.info("更新绑定设备");
        return R.successOnlyObject(signinDeviceMangerService.updateBindDeviceBasic(signinDeviceDto));
    }

    @DeleteMapping("/delete")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-device:delete")
    @NeedToken
    @ApiOperation(value = "删除在线设备", notes = "")
    public R<String> deviceDevice(String deviceId) {
        log.info("删除在线设备");
        signinDeviceMangerService.deviceDevice(deviceId);
        return R.successOnlyObject("删除成功");
    }
}
