package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;
import com.qc.printers.common.signin.service.SigninDeviceMangerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "查询已经绑定的设备", notes = "")
    public R<List<SigninDeviceDto>> list() {
        log.info("查询已经绑定的设备");
        List<SigninDeviceDto> bindDeviceList = signinDeviceMangerService.getBindDeviceList();
        log.info("{}", bindDeviceList);
        return R.success(bindDeviceList);
    }


}
