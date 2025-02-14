package com.qc.printers.custom.print.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.print.annotation.PrintDeviceRoleCheck;
import com.qc.printers.common.print.domain.dto.PrintDeviceLinkDto;
import com.qc.printers.common.print.domain.vo.PrintDeviceNotRegisterVO;
import com.qc.printers.common.print.domain.vo.request.CreatePrintDeviceReq;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceLinkQuery;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceLinkReq;
import com.qc.printers.common.print.domain.vo.request.UpdatePrintDeviceStatusReq;
import com.qc.printers.common.print.domain.vo.response.PrintDeviceVO;
import com.qc.printers.common.print.service.PrintDeviceManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController//@ResponseBody+@Controller
@RequestMapping("/print_device")
@Slf4j
@CrossOrigin("*")
@Api("打印机管理相关api")
public class PrintDeviceManagerController {
    @Autowired
    private PrintDeviceManagerService printDeviceManagerService;

    @NeedToken
    @GetMapping("/list")
    @ApiOperation(value = "获取与我相关的打印机列表")
    public R<List<PrintDeviceVO>> getPrintDeviceList() {
        return R.success(printDeviceManagerService.getPrintDeviceList());
    }
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
    @GetMapping("/link/list")
    @PrintDeviceRoleCheck(role = {1,2},deviceEl = "#params.printDeviceId")
    @ApiOperation(value = "获取打印机关联列表")
    public R<PageData<PrintDeviceLinkDto>> getPrintDeviceLinks(PrintDeviceLinkQuery params) {
        return R.success(printDeviceManagerService.getPrintDeviceLinks(params));
    }
    @NeedToken
    @GetMapping("/link/id/list")
    @PrintDeviceRoleCheck(role = {1,2},deviceEl = "#deviceId")
    @ApiOperation(value = "获取打印机关联ID列表")
    public R<Map<Integer,List<Long>>> getPrintDeviceLinkIds(@RequestParam String deviceId) {
        return R.success(printDeviceManagerService.getPrintDeviceLinkIds(Long.valueOf(deviceId)));
    }

    @NeedToken
    @PostMapping("/link/add")
    @PrintDeviceRoleCheck(role = {1,2},deviceEl = "#data.printDeviceId")
    @ApiOperation(value = "添加打印机关联")
    public R<String> addPrintDeviceLinks(@RequestBody PrintDeviceLinkReq data) {
        return R.success(printDeviceManagerService.addPrintDeviceLinks(data));
    }

    /**
     * @param printDeviceId
     * @param linkId 可以逗号分隔，批量移除
     * @return
     */
    @NeedToken
    @DeleteMapping("/link/remove")
    @PrintDeviceRoleCheck(role = {1,2},deviceEl = "#printDeviceId")
    @ApiOperation(value = "移除打印机关联")
    public R<String> removePrintDeviceLink(@RequestParam(name = "printDeviceId") String printDeviceId,
                                           @RequestParam(name = "linkId") String linkId,
                                            @RequestParam(name = "linkType") Integer linkType) {
        return R.success(printDeviceManagerService.removePrintDeviceLink(printDeviceId, linkId,linkType));
    }

    @NeedToken
    @PutMapping("/link/update_role")
    @PrintDeviceRoleCheck(role = {1},deviceEl = "#data.printDeviceId")
    @ApiOperation(value = "更新关联角色")
    public R<String> updatePrintDeviceLinkRole(@RequestBody PrintDeviceLinkReq data) {
        return R.success(printDeviceManagerService.updatePrintDeviceLinkRole(data));
    }
}
