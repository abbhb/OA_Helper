package com.qc.printers.custom.activiti.controller;


import com.qc.printers.common.activiti.entity.dto.workflow.DefinitionListDto;
import com.qc.printers.common.activiti.entity.dto.workflow.DeployProcessDto;
import com.qc.printers.common.activiti.entity.vo.workflow.DefinitionListVo;
import com.qc.printers.common.activiti.service.ProcessDefinitionService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.entity.PageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 流程维护
 *
 * @author liuguofeng
 * @date 2023/10/21 11:28
 **/
@RequestMapping("/processDefinition")
@RestController
@CrossOrigin("*")
@Slf4j
public class ProcessDefinitionController {
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 列表
     *
     * @param dto 参数
     */
    @NeedToken
    @GetMapping(value = "/list")
    public R<PageData<DefinitionListVo>> list(DefinitionListDto dto) {
        return R.success(processDefinitionService.queryPage(dto));
    }

    /**
     * 获取流程定义xml
     *
     * @param deploymentId 部署id
     */
    @GetMapping("/getDefinitionXml")
    public R<String> getDefinitionXml(String deploymentId) {
        String xml = processDefinitionService.getDefinitionXml(deploymentId);
        return R.success(xml);
    }

    /**
     * 获取流程定义详情
     *
     * @param deploymentId 部署id
     */
    @GetMapping("/getDefinitionInfo")
    public R<Map<String, Object>> getDefinitionInfo(String deploymentId) {
        Map<String, Object> result = processDefinitionService.getDefinitionInfo(deploymentId);
        return R.success(result);
    }

    /**
     * 更新流程定义状态 激活或者挂起
     *
     * @param deploymentId 部署id
     */
    @GetMapping("/updateState")
    public R<String> updateState(String deploymentId) {
        processDefinitionService.updateState(deploymentId);
        return R.success("修改成功");
    }

    /**
     * 部署流程
     *
     * @param dto 参数
     */
    @PostMapping("/deployProcess")
    public R<String> deployProcess(@Valid @RequestBody DeployProcessDto dto) {
        processDefinitionService.deployProcess(dto);
        return R.success("部署成功");
    }

    /**
     * 删除
     *
     * @param id 主键
     */
    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam("deploymentId") String id) {
        processDefinitionService.delete(id);
        return R.success("删除成功");
    }


}
