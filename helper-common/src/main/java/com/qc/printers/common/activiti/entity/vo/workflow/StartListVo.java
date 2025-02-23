package com.qc.printers.common.activiti.entity.vo.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 我发起的任务列表返回
 *
 * @author liuguofeng
 * @date 2023/11/04 14:22
 **/
@Data
public class StartListVo {
    /**
     * 流程实例id
     */
    private String id;

    /**
     * 业务key
     */
    private String businessKey;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 流程部署名称
     */
    private String definitionName;

    /**
     * 流程版本号
     */
    private Integer definitionVersion;

    /**
     * 流程key
     */
    private String definitionKey;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 1:进行中,2:已完成,3.流程被终止
     */
    private Integer status;

    /**
     * 进行中、已完成、发起人撤回、审批不通过
     * @see com.qc.printers.common.activiti.constant.TaskStatusType
     */
    private String statusName;
}
