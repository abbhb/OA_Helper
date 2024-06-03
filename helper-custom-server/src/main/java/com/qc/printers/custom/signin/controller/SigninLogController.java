package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.signin.domain.entity.SigninLog;
import com.qc.printers.common.signin.domain.resp.AddLogExtInfo;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateRealResp;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateResp;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
     * 签到机记录接口
     * 返回值带额外信息
     *
     * @return
     */
    @PostMapping("/add_log_device_plus")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    public R<AddLogExtInfo> addLogDevicePlus(@RequestBody SigninLog signinLog, HttpServletRequest request) {
        log.info("signinLog={}", signinLog);
        return R.success(signinLogService.addSigninlogByDevicePlus(request, signinLog));
    }

    /**
     * 查看某天某考勤组考勤数据报表
     * 此接口用于查询改天全部，不是实时接口[配合班次时间查出当前实际应到]
     * @return
     */
    @GetMapping("/export_signin_group_date")
    public R<SigninGroupDateResp> exportSigninGgroupDate(String groupId, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date) {
        log.info("groupId={},date = {}", groupId,date);
        return R.success(signinLogService.exportSigninGgroupDate(groupId, date));
    }


    /**
     * 查看实时某考勤组的情况，会结合当前已经激活几个班次
     * 显示当前是否处于第几个班次里或者不在打卡时段
     * 然后返回数据有所区别，
     * 包含昨日缺勤迟到早退
     * 最近班次的上班或者下班单次打卡情况，只显示有没有打卡，以及当前实时出勤人数，
     * 不在打卡时段就显示上个打卡时间段的人数，不管是不是迟到，迟到早退也统计人数
     * @param groupId
     * @return
     */
    @GetMapping("/export_signin_group_real_time")
    public R<SigninGroupDateRealResp> exportSigninGroupRealTime(String groupId) {
        log.info("groupId={}", groupId);
        return R.success(signinLogService.exportSigninGroupRealTime(groupId));
    }


}
