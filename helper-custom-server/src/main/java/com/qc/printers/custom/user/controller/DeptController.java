package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.custom.user.service.DeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController//@ResponseBody+@Controller
@RequestMapping("/dept")
@Api("和部门相关的接口")
@CrossOrigin("*")
@Slf4j
public class DeptController {

    @Autowired
    private DeptService deptService;

    @PostMapping("/list")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:dept:list")
    @NeedToken
    @ApiOperation(value = "获取全部部门", notes = "")
    public R<List<DeptManger>> list() {
        log.info("获取部门");
        List<DeptManger> deptList = deptService.getDeptList();
        log.info("userDept={}", deptList);
        return R.success(deptList);
    }

    @GetMapping("/listForBPM")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:bpm:add")
    @NeedToken
    @ApiOperation(value = "获取全部部门", notes = "")
    public R<List<DeptManger>> listForBPM(@RequestParam(required = false) String name) {
        log.info("获取部门");
        return R.success(deptService.listForBPM(name));
    }

    @GetMapping("/list-only-tree")
    @NeedToken
    @ApiOperation(value = "获取全部部门仅包含tree选择的必须项", notes = "")
    public R<List<DeptManger>> listOnlyTree() {
        log.info("获取全部部门仅包含tree选择的必须项");
        List<DeptManger> deptList = deptService.getDeptListOnlyTree();
        log.info("userDept={}", deptList);
        return R.success(deptList);
    }

    @PostMapping("/add")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:dept:add")
    @NeedToken
    @ApiOperation(value = "添加部门", notes = "")
    public R<String> add(@RequestBody DeptManger deptManger) {
        log.info("添加部门");
        log.info("deptManger={}", deptManger);
        return R.successOnlyObject(deptService.addDept(deptManger));
    }

    @PutMapping("/update")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:dept:update")
    @NeedToken
    @ApiOperation(value = "update部门", notes = "")
    public R<String> update(@RequestBody DeptManger deptManger) {
        log.info("update部门");
        log.info("deptManger={}", deptManger);
        return R.successOnlyObject(deptService.updateDept(deptManger));
    }

    @DeleteMapping("/delete")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:dept:delete")
    @NeedToken
    @ApiOperation(value = "删除部门", notes = "")
    public R<String> delete(String id) {
        log.info("删除部门.{}", id);
        if (StringUtils.isEmpty(id)) {
            return R.error("参数不全");
        }
        return R.successOnlyObject(deptService.deleteDept(id));

    }
}
