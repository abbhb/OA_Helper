package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.custom.signin.domain.req.SigninUserFaceDataReq;
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


}
