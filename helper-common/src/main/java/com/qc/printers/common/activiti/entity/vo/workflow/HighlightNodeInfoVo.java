package com.qc.printers.common.activiti.entity.vo.workflow;

import com.qc.printers.common.activiti.constant.NodeStatus;
import lombok.Data;

import java.util.List;

/**
 * 高亮节点信息
 *
 * @author liuguofeng
 * @date 2023/11/06 13:53
 **/
@Data
public class HighlightNodeInfoVo {
    /**
     * 流程定义节点唯一标识
     */
    private String activityId;

    /**
     * 状态 1:已完成节点,2:活动的未处理的节点(下一个节点), 参考 {@link NodeStatus}
     */
    private Integer status;

    /**
     * 历史审批记录
     */
    private List<HistoryRecordVo> historyRecordVo;
}
