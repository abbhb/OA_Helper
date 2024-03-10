package com.qc.printers.common.activiti.entity.vo.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qc.printers.common.activiti.constant.NodeStatus;
import lombok.Data;

import java.util.Date;

/**
 * 流程记录
 *
 * @author liuguofeng
 * @date 2023/11/05 15:02
 **/
@Data
public class HistoryRecordVo {
    /**
     * 任务名称
     */
    private String nodeName;

    /**
     * 流程定义节点唯一标识
     */
    private String activityId;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 状态 1:已完成节点,2:活动的未处理的节点(下一个节点) 参考{@link NodeStatus}
     */
    private Integer status;

    /**
     * 流程记录身份信息
     */
    private IdentityVo identity;

    /**
     * 动态表单结构数据
     */
    private Object formJson;

    /**
     * 动态表单数据
     */
    private Object formData;
}
