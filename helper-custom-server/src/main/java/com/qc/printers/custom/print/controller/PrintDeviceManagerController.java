package com.qc.printers.custom.print.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.print.annotation.PrintDeviceRoleCheck;
import com.qc.printers.common.print.domain.dto.PrintDeviceUserDto;
import com.qc.printers.common.print.domain.vo.PrintDeviceNotRegisterVO;
import com.qc.printers.common.print.domain.vo.request.CreatePrintDeviceReq;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceUserQuery;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceUserReq;
import com.qc.printers.common.print.domain.vo.request.UpdatePrintDeviceStatusReq;
import com.qc.printers.common.print.service.PrintDeviceManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController//@ResponseBody+@Controller
@RequestMapping("/print_device")
@Slf4j
@CrossOrigin("*")
@Api("打印机管理相关api")
public class PrintDeviceManagerController {
    @Autowired
    private PrintDeviceManagerService printDeviceManagerService;

    @NeedToken
    @GetMapping("/unregister/list")
    @ApiOperation(value = "获取未注册的在线打印机列表")
    public R<List<PrintDeviceNotRegisterVO>> getUnRegisterPrintDeviceList() {
        return R.success(printDeviceManagerService.getUnRegisterPrintDeviceList());
    }

    @NeedToken
    @PostMapping("/create")
    @ApiOperation(value = "注册打印机")
    public R<String> createPrintDevice(@RequestBody CreatePrintDeviceReq data) {
        return R.success(printDeviceManagerService.createPrintDevice(data));
    }
    @NeedToken
    @PutMapping("/update_status")
    @PrintDeviceRoleCheck(role = {1,2},deviceEl = "#data.id")
    @ApiOperation(value = "更新打印机状态")
    public R<String> updatePrintDeviceStatus(@RequestBody UpdatePrintDeviceStatusReq data) {
        return R.success(printDeviceManagerService.updatePrintDeviceStatus(data));
    }

    @NeedToken
    @DeleteMapping("/delete")
    @PrintDeviceRoleCheck(role = {1},deviceEl = "#id")
    @ApiOperation(value = "删除打印机")
    public R<String> deletePrintDevice(@RequestParam(name = "id") String id) {
        return R.success(printDeviceManagerService.deletePrintDevice(id));
    }

    @NeedToken
    @GetMapping("/user/list")
    @ApiOperation(value = "获取打印机用户列表")
    public R<PageData<PrintDeviceUserDto>> getPrintDeviceUsers(PrintDeviceUserQuery params) {
        return R.success(printDeviceManagerService.getPrintDeviceUsers(params));
    }

    @NeedToken
    @PostMapping("/user/add")
    @PrintDeviceRoleCheck(role = {1,2},deviceEl = "#data.printDeviceId")
    @ApiOperation(value = "添加打印机用户")
    public R<String> addPrintDeviceUsers(@RequestBody PrintDeviceUserReq data) {
        return R.success(printDeviceManagerService.addPrintDeviceUsers(data));
    }

    /**
     * @param printDeviceId
     * @param userId 可以逗号分隔，批量移除
     * @return
     */
    @NeedToken
    @DeleteMapping("/user/remove")
    @PrintDeviceRoleCheck(role = {1,2},deviceEl = "#printDeviceId")
    @ApiOperation(value = "移除打印机用户")
    public R<String> removePrintDeviceUser(@RequestParam(name = "printDeviceId") String printDeviceId,
                                           @RequestParam(name = "userId") String userId) {
        return R.success(printDeviceManagerService.removePrintDeviceUser(printDeviceId,userId));
    }

    @NeedToken
    @PutMapping("/user/update_role")
    @PrintDeviceRoleCheck(role = {1},deviceEl = "#data.printDeviceId")
    @ApiOperation(value = "更新用户角色")
    public R<String> updatePrintDeviceUserRole(@RequestBody PrintDeviceUserReq data) {
        return R.success(printDeviceManagerService.updatePrintDeviceUserRole(data));
    }
}
