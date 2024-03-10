package com.qc.printers.common.activiti.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qc.printers.common.activiti.utils.handler.NodeColumnTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 流程部署节点数据
 *
 * @author liuguofeng
 * @email liuguofeng-java@qq.com
 * @date 2023-11-28 14:50:18
 */
@Data
@TableName(value = "sys_deploy_node", autoResultMap = true)
public class SysDeployNodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点id
     */
    @TableId
    private String nodeId;
    /**
     * 部署id
     */
    private String deployId;
    /**
     * 流程定义节点唯一标识
     */
    private String activityId;
    /**
     * 表单详情
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> formJson;
    /**
     * 表单详情
     */
    @TableField(typeHandler = NodeColumnTypeHandler.class)
    private List<NodeColumnItem> columns;
    /**
     * 是否是主表单,1:是,2:否
     */
    private Integer isMainFrom;
    /**
     * 创建时间
     */
    private Date createTime;

}
