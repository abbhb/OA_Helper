package com.qc.printers.common.activiti.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.activiti.entity.SysDeployEntity;

import java.util.Map;

/**
 * 流程部署详情
 */
public interface SysDeployService extends IService<SysDeployEntity> {
    /**
     * 插入数据到绑定数据库表中
     *
     * @param instanceId 实例id
     * @param deployId   部署id
     * @param activityId 流程定义节点唯一标识
     * @param variables  流程变量
     */
    void saveData(String instanceId, String deployId, String activityId, Map<String, Object> variables);

}

