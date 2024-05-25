package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.signin.domain.entity.SigninLog;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateResp;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

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

    /**
     * 查看某天某考勤组考勤数据报表
     * 此接口用于查询改天全部，不是实时接口[配合班次时间查出当前实际应到]
     * @return
     */
    @GetMapping("/export_signin_group_date")
    public R<SigninGroupDateResp> exportSigninGgroupDate(String groupId, LocalDate date) {
        log.info("groupId={},date = {}", groupId,date);
        return R.success(signinLogService.exportSigninGgroupDate(groupId, date));
    }

}
