package com.qc.printers.common.activiti.entity.dto.workflow;

import lombok.Data;

/**
 * 我发起任务列表参数
 *
 * @author liuguofeng
 * @date 2023/11/04 14:11
 **/
@Data
public class TodoListDto {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 部门id(组id)
     */
    private String deptId;

    /**
     * 流程定义名称
     */
    private String definitionName;

    /**
     * 流程定义key
     */
    private String definitionKey;
    private Integer pageNum;
    private Integer pageSize;
}
