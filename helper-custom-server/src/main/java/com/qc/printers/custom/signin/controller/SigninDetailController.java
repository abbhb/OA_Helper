package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.annotation.SigninDetailUserCheck;
import com.qc.printers.common.signin.domain.entity.SigninBc;
import com.qc.printers.custom.signin.domain.vo.*;
import com.qc.printers.custom.signin.service.SigninDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signin_detail")
@Api("考勤详情相关接口")
@CrossOrigin("*")
@Slf4j
public class SigninDetailController {

    @Autowired
    private SigninDetailService signinDetailService;
    /*
     该接口下的所有接口都需要加上数据权限过滤
     */

    /**
     * 获取用户基础信息
     * @param userId
     * @return
     */
    @GetMapping("/get_user_info/{userId}")
    @NeedToken
    @SigninDetailUserCheck(userIdKey = "#userId")
    @ApiOperation(value = "获取考勤用户基础信息", notes = "")
    public R<SigninDetailUserInfoResp> getUserInfo(@PathVariable(name = "userId") Long userId) {
        log.info("获取考勤用户基础信息");
        return R.success(signinDetailService.getUserInfo(userId));
    }
    /**
     * 获取某日考勤基础信息
     * @param userId
     * @param date 2024-03-16
     * @return
     */
    @GetMapping("/get_signin_info/{userId}")
    @NeedToken
    @SigninDetailUserCheck(userIdKey = "#userId")
    @ApiOperation(value = "获取某日考勤基础信息", notes = "")
    public R<SigninDetailDataResp> getSigninInfo(@PathVariable(name = "userId") Long userId, String date) {
        log.info("获取某日考勤基础信息");
        return R.success(signinDetailService.getSigninInfo(userId,date));
    }


    /**
     * 获取某日考勤打卡信息
     * @param userId
     * @param date 2024-03-16
     * @return getSigninDetailSigninInfos
     */
    @GetMapping("/get_signin_detail_signin_infos/{userId}")
    @NeedToken
    @SigninDetailUserCheck(userIdKey = "#userId")
    @ApiOperation(value = "获取某日考勤打卡信息", notes = "")
    public R<List<SigninDetailSigninInfoResp>> getSigninDetailSigninInfos(@PathVariable(name = "userId") Long userId, String date) {
        log.info("获取某日考勤打卡信息");
        return R.success(signinDetailService.getSigninDetailSigninInfos(userId,date));
    }

    /**
     * 获取某日打卡数据
     * @param userId
     * @param date
     * @return
     */
    @GetMapping("/get_clocking_records/{userId}")
    @NeedToken
    @SigninDetailUserCheck(userIdKey = "#userId")
    @ApiOperation(value = "获取某日考勤打卡记录", notes = "")
    public R<List<SigninDetailClockingDataResp>> getClockingRecords(@PathVariable(name = "userId") Long userId, String date) {
        log.info("获取某日考勤打卡记录");
        return R.success(signinDetailService.getClockingRecords(userId,date));
    }

    /**
     * 获取某日补签数据
     */
    @GetMapping("/get_supplement_records/{userId}")
    @NeedToken
    @SigninDetailUserCheck(userIdKey = "#userId")
    @ApiOperation(value = "获取某日考勤补签记录", notes = "")
    public R<List<SigninDetailSupplementDataResp>> getSupplementRecords(@PathVariable(name = "userId") Long userId, String date) {
        log.info("获取某日考勤补签记录");
        return R.success(signinDetailService.getSupplementRecords(userId,date));
    }
}
