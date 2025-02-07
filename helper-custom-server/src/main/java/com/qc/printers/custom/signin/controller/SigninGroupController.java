package com.qc.printers.custom.signin.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.holidays.domain.Holidays;
import com.qc.printers.common.signin.domain.dto.SigninGroupDto;
import com.qc.printers.common.signin.service.SigninGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
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
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-group:add")
    @NeedToken
    @ApiOperation(value = "添加考勤组", notes = "")
    public R<String> add(@RequestBody SigninGroupDto signinGroupDto) {
        log.info("添加考勤组");
        log.info("signinGroupDto={}", signinGroupDto);
        return R.success(signinGroupService.addSigninGroup(signinGroupDto));
    }


    @DeleteMapping("/delete")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-group:delete")
    @NeedToken
    @ApiOperation(value = "删除考勤组", notes = "")
    public R<String> delete(String id) {
        log.info("删除考勤组");
        return R.success(signinGroupService.deleteSigninGroup(id));
    }

    @PutMapping("/update")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-group:update")
    @NeedToken
    @ApiOperation(value = "更新考勤组规则信息", notes = "")
    public R<String> update(@RequestBody SigninGroupDto signinGroupDto) {
        log.info("更新考勤组");
        return R.success(signinGroupService.updateSigninGroup(signinGroupDto));
    }

    @GetMapping("/list")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-group:list")
    @NeedToken
    @ApiOperation(value = "获取考勤组规则信息", notes = "")
    public R<List<SigninGroupDto>> list() {
        log.info("获取考勤组规则信息");
        return R.success(signinGroupService.listSigninGroup());
    }

    /*
     * 节假日相关
     * 开放权限
     */
    @GetMapping("/list_holidays/{group_id}")
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-group-holiday:list")
    @NeedToken
    @ApiOperation(value = "获取考勤组节假日信息")
    public R<List<Holidays>> listHolidays(@PathVariable("group_id") Long groupId,
                                          @RequestParam
                                          @DateTimeFormat(pattern = "yyyy-MM-dd")
                                          @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
                                          LocalDate startDate,
                                          @RequestParam
                                          @DateTimeFormat(pattern = "yyyy-MM-dd")
                                          @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
                                          LocalDate endDate) {
        log.info("获取考勤组节假日信息,group_id:{},startDate:{},endDate:{}",groupId,startDate,endDate);
        return R.success(signinGroupService.listHolidays(groupId,startDate,endDate));
    }

    @PostMapping("/update_holidays/{group_id}")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-group-holiday:update")
    @NeedToken
    @ApiOperation(value = "更新考勤组节假日信息")
    public R<String> updateHolidays(@PathVariable("group_id") Long groupId,
                                    @RequestBody
                                    Holidays holidays) {
        log.info("更新考勤组节假日信息,group_id:{},holidays:{}",groupId,holidays);
        return R.success(signinGroupService.updateHolidays(groupId,holidays));
    }

    @DeleteMapping("/delete_holidays/{group_id}/{id}")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:signin-group-holiday:delete")
    @NeedToken
    @ApiOperation(value = "删除考勤组节假日信息")
    public R<String> deleteHolidays(@PathVariable("group_id") Long groupId,@PathVariable("id") Long id) {
        log.info("删除考勤组节假日信息,group_id:{}",groupId);
        return R.success(signinGroupService.deleteHolidays(groupId,id));
    }

}
