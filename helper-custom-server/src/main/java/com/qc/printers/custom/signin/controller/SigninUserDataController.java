package com.qc.printers.custom.signin.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.poi.ExcelUtil;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.signin.domain.dto.SigninUserDataExcelDto;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.signin.domain.req.SigninUserCardDataReq;
import com.qc.printers.custom.signin.domain.req.SigninUserFaceDataReq;
import com.qc.printers.custom.signin.domain.vo.SigninDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserCardDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;
import com.qc.printers.custom.signin.service.SigninUserDataService;
import com.qc.printers.custom.user.domain.vo.response.UserResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/signin_user_data")
@Api("签到数据相关的接口")
@CrossOrigin("*")
@Slf4j
public class SigninUserDataController {
    @Autowired
    private MinIoProperties minIoProperties;
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


    @GetMapping("/data_manger")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_data:list")
    @ApiOperation(value = "获取所有用户的用户数据", notes = "")
    public R<PageData<SigninDataResp>> DataManger(Integer pageNum, Integer pageSize, @RequestParam(required = false, name = "name") String name, @RequestParam(required = false, name = "cascade", defaultValue = "0") Integer cascade, @RequestParam(required = false, name = "deptId") Long deptId) {
        log.info("用户管理获取所有用户");
        return R.success(signinUserDataService.getDataMangerList(pageNum, pageSize, name, cascade, deptId));
    }
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_data:export")
    @PostMapping("/export")
    @ApiOperation(value = "导出数据", notes = "")
    public R<String> export()
    {
        List<SigninUserDataExcelDto> list = signinUserDataService.exportAllData();
        ExcelUtil<SigninUserDataExcelDto> util = new ExcelUtil<SigninUserDataExcelDto>(SigninUserDataExcelDto.class,minIoProperties.getBucketName());
        return util.exportExcel(list, "生物数据");
    }

    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_data:import")
    @PostMapping("/importData")
    @ApiOperation(value = "导入数据", notes = "")
    public R<String> importData(MultipartFile file,boolean updateSupport) throws Exception
    {
        ExcelUtil<SigninUserDataExcelDto> util = new ExcelUtil<SigninUserDataExcelDto>(SigninUserDataExcelDto.class,minIoProperties.getBucketName());
        List<SigninUserDataExcelDto> dataList = util.importExcel(file.getInputStream());
        String s = signinUserDataService.importSigninUserCardData(dataList, updateSupport);
        log.info("dattas{},updateSupport{}",dataList,updateSupport);

        return R.successOnlyObject(s);
    }
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin_data:import")
    @GetMapping("/importTemplate")
    @ApiOperation(value = "导入模板", notes = "")
    public R<String> importTemplate()
    {
        ExcelUtil<SigninUserDataExcelDto> util = new ExcelUtil<SigninUserDataExcelDto>(SigninUserDataExcelDto.class,minIoProperties.getBucketName());
        return util.importTemplateExcel("用户数据");
    }

}
