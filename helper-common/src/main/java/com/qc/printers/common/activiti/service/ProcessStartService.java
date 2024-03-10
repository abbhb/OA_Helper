package com.qc.printers.common.activiti.service;


import com.qc.printers.common.activiti.entity.dto.workflow.StartListDto;
import com.qc.printers.common.activiti.entity.dto.workflow.StartProcessDto;
import com.qc.printers.common.common.domain.entity.PageData;

/**
 * 流程启动
 **/
public interface ProcessStartService {

    /**
     * 我发起的任务列表
     *
     * @param dto 参数
     * @return 结果
     */
    PageData queryPage(StartListDto dto);

    /**
     * 启动流程
     *
     * @param dto    启动流程参数
     * @param userId 当前用户登录id
     */
    void startProcess(StartProcessDto dto, String userId);

    /**
     * 删除流程实例
     *
     * @param instanceId 流程实例id
     */
    void delete(String instanceId);

}
