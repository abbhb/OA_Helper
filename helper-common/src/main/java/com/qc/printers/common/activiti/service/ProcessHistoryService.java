package com.qc.printers.common.activiti.service;


import com.qc.printers.common.activiti.entity.dto.workflow.FinishedListDto;
import com.qc.printers.common.activiti.entity.vo.workflow.HistoryRecordVo;
import com.qc.printers.common.common.domain.entity.PageData;

import java.util.List;
import java.util.Map;

/**
 * 流程历史相关
 *
 * @author liuguofeng
 * @date 2023/11/24 09:31
 **/
public interface ProcessHistoryService {

    /**
     * 已办任务列表
     *
     * @param dto 参数
     * @return 结果
     */
    PageData queryPage(FinishedListDto dto);

    /**
     * 查询审批进度
     *
     * @param instanceId 流程实例id
     * @return 审批记录
     */
    List<HistoryRecordVo> getHistoryRecord(String instanceId);

    /**
     * 查询流程图信息(高亮信息)
     *
     * @param instanceId 流程实例id
     * @return 流程图信息
     */
    Map<String, Object> getHighlightNodeInfo(String instanceId);

    /**
     * 获取主表单信息
     *
     * @param instanceId 流程实例id
     * @return 主表单数据
     */
    Map<String, Object> getMainFormInfo(String instanceId);

}
