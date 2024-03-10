package com.qc.printers.common.activiti.service;


import cn.hutool.db.Page;
import com.qc.printers.common.activiti.entity.dto.workflow.DefinitionListDto;
import com.qc.printers.common.activiti.entity.dto.workflow.DeployProcessDto;
import com.qc.printers.common.common.domain.entity.PageData;

import java.util.Map;

/**
 * 流程定义
 *
 * @author liuguofeng
 * @date 2023/10/21 11:31
 **/
public interface ProcessDefinitionService {

    /**
     * 流程管理列表
     *
     * @param dto 参数
     * @return 列表
     */
    PageData queryPage(DefinitionListDto dto);

    /**
     * 获取流程定义xml
     *
     * @param deploymentId 部署id
     * @return 流程xml字符串
     */
    String getDefinitionXml(String deploymentId);

    /**
     * 获取流程定义详情
     *
     * @param deploymentId 部署id
     * @return 流程xml字符串和流程表单
     */
    Map<String, Object> getDefinitionInfo(String deploymentId);

    /**
     * 更新流程定义状态 激活或者挂起
     *
     * @param deploymentId 部署id
     */
    void updateState(String deploymentId);

    /**
     * 部署流程
     *
     * @param dto 参数
     */
    void deployProcess(DeployProcessDto dto);

    /**
     * 删除流程
     *
     * @param deploymentId 部署id
     */
    void delete(String deploymentId);

}
