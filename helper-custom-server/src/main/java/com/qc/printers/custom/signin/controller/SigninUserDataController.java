package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.custom.signin.domain.req.SigninUserCardDataReq;
import com.qc.printers.custom.signin.domain.req.SigninUserFaceDataReq;
import com.qc.printers.custom.signin.domain.vo.SigninUserCardDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;
import com.qc.printers.custom.signin.service.SigninUserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signin_user_data")
@Api("签到数据相关的接口")
@CrossOrigin("*")
@Slf4j
public class SigninUserDataController {

    @Autowired
    private SigninUserDataService signinUserDataService;

    @GetMapping("/get_signin_face_data")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "同步人脸数据列表", notes = "")
    public R<List<SigninUserFaceDataResp>> getSigninFaceData(String deviceId) {
        log.info("同步人脸数据列表");
        return R.success(signinUserDataService.getSigninFaceData(deviceId));
    }

    @PostMapping("/upload_signin_face_data")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "同步人脸数据列表", notes = "")
    public R<String> uploadSigninFaceData(@RequestBody SigninUserFaceDataReq signinUserFaceDataReq) {
        log.info("向打卡机上传人脸列表");
        return R.success(signinUserDataService.uploadSigninFaceData(signinUserFaceDataReq));
    }

    @PostMapping("/download_signin_face_data")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "同步人脸数据列表", notes = "")
    public R<String> downloadSigninFaceData(@RequestBody SigninUserFaceDataReq signinUserFaceDataReq) {
        log.info("从打卡机下载人脸列表");
        return R.success(signinUserDataService.downloadSigninFaceData(signinUserFaceDataReq));
    }


    /**
     * Card方式
     */

    @GetMapping("/get_signin_card_data")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "同步ID卡数据列表", notes = "")
    public R<List<SigninUserCardDataResp>> getSigninCardData(String deviceId) {
        log.info("同步ID卡数据列表");
        return R.success(signinUserDataService.getSigninCardData(deviceId));
    }

    @PostMapping("/upload_signin_card_data")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "同步ID数据列表", notes = "")
    public R<String> uploadSigninCardData(@RequestBody SigninUserCardDataReq signinUserCardDataReq) {
        log.info("同步ID数据列表");
        return R.success(signinUserDataService.uploadSigninCardData(signinUserCardDataReq));
    }

    @PostMapping("/download_signin_card_data")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_group:add")
    @NeedToken
    @ApiOperation(value = "同步ID卡数据列表", notes = "")
    public R<String> downloadSigninCardData(@RequestBody SigninUserCardDataReq signinUserCardDataReq) {
        log.info("同步ID卡数据列表");
        return R.success(signinUserDataService.downloadSigninCardData(signinUserCardDataReq));
    }


}
