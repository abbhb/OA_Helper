package com.qc.printers.custom.activiti.controller;

import com.qc.printers.common.activiti.entity.DeployGroup;
import com.qc.printers.common.activiti.entity.DeployGroupAct;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.custom.activiti.pojo.dto.DeployGroupActDto;
import com.qc.printers.custom.activiti.service.DeployGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/deployGroup")
@CrossOrigin("*")
@RestController
public class DeployGroupController {

    @Autowired
    private DeployGroupService deployGroupService;

    @NeedToken
    @PermissionCheck(role = {"admin"}, permission = "sys:bpm-group:list")
    @GetMapping("/listDeployGroup")
    public R<List<DeployGroup>> getDeployGroupList() {
        return R.success(deployGroupService.getDeployGroupList());
    }

    @NeedToken
    @PermissionCheck(role = {"admin"}, permission = "sys:bpm-group:add")
    @PostMapping("/addDeployGroup")
    public R<String> addDeployGroup(@RequestBody DeployGroup deployGroup) {
        deployGroupService.addDeployGroup(deployGroup);
        return R.success("添加成功");
    }

    @NeedToken
    @PermissionCheck(role = {"admin"}, permission = "sys:bpm-group:update")
    @PutMapping("/putDeployGroup")
    public R<String> putDeployGroup(@RequestBody DeployGroup deployGroup) {
        deployGroupService.putDeployGroup(deployGroup);
        return R.success("修改成功");
    }

    @NeedToken
    @PermissionCheck(role = {"admin"}, permission = "sys:bpm-group:delete")
    @DeleteMapping("/deleteDeployGroup")
    public R<String> deleteDeployGroup(Long id) {
        deployGroupService.deleteDeployGroup(id);
        return R.success("删除成功");
    }


    @NeedToken
    @PermissionCheck(role = {"admin"}, permission = "sys:bpm-group-act:list")
    @GetMapping("/listDeployGroupAct")
    public R<List<DeployGroupActDto>> getDeployGroupActList() {
        return R.success(deployGroupService.getDeployGroupActList());
    }
//    @NeedToken
//    @PermissionCheck(role = {"admin"},permission = "sys:bpm-group-act:add")
//    @PostMapping("/addDeployGroupAct")
//    public R<String> addDeployGroupAct(@RequestBody DeployGroupAct deployGroupAct) {
//        deployGroupService.addDeployGroupAct(deployGroupAct);
//        return R.success("添加成功");
//    }

    /**
     * 默认就是未分组，没有id就是未分组
     *
     * @param deployGroupAct
     * @return
     */
    @NeedToken
    @PermissionCheck(role = {"admin"}, permission = "sys:bpm-group-act:update")
    @PutMapping("/putDeployGroupAct")
    public R<String> putDeployGroupAct(@RequestBody DeployGroupAct deployGroupAct) {
        deployGroupService.putDeployGroupAct(deployGroupAct);
        return R.success("修改成功");
    }

    @NeedToken
    @PermissionCheck(role = {"admin"}, permission = "sys:bpm-group-act:delete")
    @DeleteMapping("/deleteDeployGroupAct")
    public R<String> deleteDeployGroupAct(Long id) {
        deployGroupService.deleteDeployGroupAct(id);
        return R.success("删除成功");
    }


}
