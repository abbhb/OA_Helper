package com.qc.printers.common.activiti.service;


import com.qc.printers.common.activiti.entity.dto.workflow.TodoCompleteDto;
import com.qc.printers.common.activiti.entity.dto.workflow.TodoListDto;
import com.qc.printers.common.common.domain.entity.PageData;

import java.util.Map;

/**
 * 代办任务
 **/
public interface ProcessTodoService {

    /**
     * 查看我代办的流程
     *
     * @param dto 参数
     */
    PageData queryPage(TodoListDto dto);

    /**
     * 获取节点表单
     *
     * @param taskId 任务id
     * @return 表单数据
     */
    Map<String, Object> getNodeForm(String taskId);

    /**
     * 节点审批
     *
     * @param dto 参数
     */
    void complete(TodoCompleteDto dto);

}
