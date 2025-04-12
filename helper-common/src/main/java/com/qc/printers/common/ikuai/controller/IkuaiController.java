package com.qc.printers.common.ikuai.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.ikuai.domain.dto.SysIkuaiNetAllowLinkDto;
import com.qc.printers.common.ikuai.domain.vo.req.IkuaiAllowLinkQuery;
import com.qc.printers.common.ikuai.domain.vo.req.IkuaiAllowLinkReq;
import com.qc.printers.common.ikuai.service.IkuaiService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController//@ResponseBody+@Controller
@RequestMapping("/ikuai_controller")
@Api("Ikuai相关接口")
@CrossOrigin("*")
@Slf4j
public class IkuaiController {

    @Autowired
    private IkuaiService ikuaiService;
    @NeedToken
    @GetMapping("/link/list")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:ikuai_allow:list")
    @ApiOperation(value = "获取Ikuai关联列表")
    public R<PageData<SysIkuaiNetAllowLinkDto>> getPrintDeviceLinks(IkuaiAllowLinkQuery params) {
        return R.success(ikuaiService.getIkuaiAllowLinks(params));
    }
    @NeedToken
    @GetMapping("/link/id/list")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:ikuai_allow:list")
    @ApiOperation(value = "获取Ikuai关联ID列表")
    public R<Map<Integer, List<Long>>> getPrintDeviceLinkIds() {
        return R.success(ikuaiService.getIkuaiAllowLinkIds());
    }

    @NeedToken
    @PostMapping("/link/add")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:ikuai_allow:add")
    @ApiOperation(value = "添加Ikuai关联")
    public R<String> addPrintDeviceLinks(@RequestBody IkuaiAllowLinkReq data) {
        return R.success(ikuaiService.addIkuaiAllowLinks(data));
    }

    /**
     * @param linkId 可以逗号分隔，批量移除
     * @return
     */
    @NeedToken
    @DeleteMapping("/link/remove")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:ikuai_allow:delete")
    @ApiOperation(value = "移除Ikuai关联")
    public R<String> removePrintDeviceLink(@RequestParam(name = "linkId") String linkId,
                                           @RequestParam(name = "linkType") Integer linkType) {
        return R.success(ikuaiService.removeIkuaiAllowLink(linkId,linkType));
    }
}
