package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.signin.domain.entity.SigninLog;
import com.qc.printers.common.signin.service.SigninLogService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/signin_log")
@Api("签到记录相关的接口")
@CrossOrigin("*")
@Slf4j
public class SigninLogController {
    @Autowired
    private SigninLogService signinLogService;

    /**
     * 签到机记录接口
     *
     * @return
     */
    @PostMapping("/add_log_device")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    public R<String> addLogDevice(@RequestBody SigninLog signinLog, HttpServletRequest request) {
        log.info("signinLog={}", signinLog);
        return R.success(signinLogService.addSigninlogByDevice(request, signinLog));
    }
}
