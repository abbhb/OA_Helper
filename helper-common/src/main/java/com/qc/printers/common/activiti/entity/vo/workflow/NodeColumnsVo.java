package com.qc.printers.common.activiti.entity.vo.workflow;

import com.qc.printers.common.activiti.entity.NodeColumnItem;
import lombok.Data;

import java.util.List;

/**
 * 节点绑定数据库表字段的数据
 *
 * @author liuguofeng
 * @date 2023/12/15 10:33
 **/
@Data
public class NodeColumnsVo {
    /**
     * 流程定义节点唯一标识
     */
    private String activityId;

    /**
     * 绑定的具体字段
     */
    private List<NodeColumnItem> columns;
}
