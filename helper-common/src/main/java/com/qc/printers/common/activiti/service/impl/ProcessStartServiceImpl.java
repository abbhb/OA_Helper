package com.qc.printers.common.activiti.service.impl;


import com.qc.printers.common.activiti.constant.ActivityType;
import com.qc.printers.common.activiti.constant.Constants;
import com.qc.printers.common.activiti.constant.TaskDeleteType;
import com.qc.printers.common.activiti.constant.TaskStatusType;
import com.qc.printers.common.activiti.entity.dto.workflow.StartListDto;
import com.qc.printers.common.activiti.entity.dto.workflow.StartProcessDto;
import com.qc.printers.common.activiti.entity.vo.workflow.StartListVo;
import com.qc.printers.common.activiti.service.ProcessStartService;
import com.qc.printers.common.activiti.service.SysDeployService;
import com.qc.printers.common.activiti.service.strategy.AssigneeLeaderHandelFactory;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.entity.PageData;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流程启动
 *
 **/
@Slf4j
@Service("processStartService")
public class ProcessStartServiceImpl implements ProcessStartService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;


    @Autowired
    private SysDeployService deployService;

    /**
     * 我发起的任务列表
     *
     * @param dto 参数
     * @return 结果
     */
    @Override
    public PageData queryPage(StartListDto dto) {

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(dto.getUserId())
                .orderByProcessInstanceStartTime()
                .desc();

        // 根据业务key查询 注意是等于不是模糊查询
        if (StringUtils.isNoneEmpty(dto.getBusinessKey())) {
            query.processInstanceBusinessKey(dto.getBusinessKey());
        }
        // 根据流程名称查询 注意是等于不是模糊查询
        if (StringUtils.isNoneEmpty(dto.getDefinitionName())) {
            query.processDefinitionName(dto.getDefinitionName());
        }
        // 根据流程key查询 注意是等于不是模糊查询
        if (StringUtils.isNoneEmpty(dto.getDefinitionKey())) {
            query.processDefinitionKey(dto.getDefinitionKey());
        }

        List<HistoricProcessInstance> list = query
                .listPage(dto.getPageNum() - 1, dto.getPageSize());
        List<StartListVo> resultList = new ArrayList<>();
        for (HistoricProcessInstance item : list) {
            // 设置流程实例
            StartListVo vo = new StartListVo();
            resultList.add(vo);
            vo.setId(item.getId());
            vo.setBusinessKey(item.getBusinessKey());
            vo.setStartTime(item.getStartTime());
            vo.setEndTime(item.getEndTime());

            // 流程定义信息
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(item.getProcessDefinitionId())
                    .singleResult();
            vo.setDefinitionName(definition.getName());
            vo.setDefinitionKey(definition.getKey());
            vo.setDefinitionVersion(definition.getVersion());

            // 获取任务处理节点
            Task task = taskService.createTaskQuery()
                    .processInstanceId(item.getId())
                    .singleResult();
            if (task != null) {
                vo.setTaskId(task.getId());
                vo.setTaskName(task.getName());
                vo.setStatus(1);
                vo.setStatusName(TaskStatusType.IN_PROGRESS);
                // 任务进行中
                continue;
            }
            // 任务处理完成在history获取
            List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(item.getId())
                    .orderByHistoricTaskInstanceEndTime()
                    .desc()
                    .list();
            HistoricTaskInstance historicTask = historicTaskInstances.get(0);
            vo.setStatus(2);
            vo.setStatusName(TaskStatusType.COMPLETED);
            vo.setTaskName("结束节点");
            // 没有结束节点还已完成说明流程是被删除的
            if (StringUtils.isEmpty(item.getEndActivityId())) {
                // 没有结束节点还已完成说明被撤回了
                log.info("流程被撤回了 {}",historicTask);
                // deleteReason获取
                String deleteReason = item.getDeleteReason();
                if (StringUtils.isEmpty(deleteReason)){
                    // 兼容老版本，默认就是被撤销
                    deleteReason = TaskDeleteType.CheXiao;
                }
                if (deleteReason.equals(TaskDeleteType.BuTongGuo)){
                    vo.setStatusName(TaskStatusType.REJECT);
                }else if (deleteReason.equals(TaskDeleteType.CheXiao)){
                    vo.setStatusName(TaskStatusType.RECALL);
                }else {
                    vo.setStatusName(deleteReason);
                }
                // 终止状态
                vo.setStatus(3);
                vo.setTaskName(historicTask.getName());
            }
            vo.setTaskId(historicTask.getId());
        }
        PageData pageData = new PageData();
        pageData.setTotal(query.count());
        pageData.setRecords(resultList);
        return pageData;
    }


    /**
     * 启动流程
     *
     * @param dto    启动流程参数
     * @param userId 当前用户登录id
     */
    @Transactional
    @Override
    public void startProcess(StartProcessDto dto, String userId) {
        // 获取相关数据
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(dto.getDefinitionId()).singleResult();
        if (definition.isSuspended()) throw new CustomException("流程已挂起,不能启动!");
        // 设置流程发起人用户Id
        Authentication.setAuthenticatedUserId(userId);
        Map<String, Object> variables = dto.getVariables();
        // 设置发起人用户id
        // 如果节点审批人,设置的是发起人,则审批节点的 assignee="${initiator}"
        variables.put(Constants.PROCESS_INITIATOR, userId);
        // todo:如果本来就没有这个，也不影响会导致无法发起
        AssigneeLeaderHandelFactory.getStrategyNoNull(Constants.PROCESS_ASSIGNEELEADER_0).addVariables(variables, userId);
        AssigneeLeaderHandelFactory.getStrategyNoNull(Constants.PROCESS_ASSIGNEELEADER_1).addVariables(variables, userId);
        AssigneeLeaderHandelFactory.getStrategyNoNull(Constants.PROCESS_ASSIGNEELEADER_2).addVariables(variables, userId);
        ProcessInstance instance = runtimeService.startProcessInstanceById(dto.getDefinitionId(), dto.getBusinessKey(), variables);

        // 获取开始事件
        HistoricActivityInstance activityInstance = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instance.getId()).list()
                .stream().filter(t -> t.getActivityType().equals(ActivityType.START_EVENT))
                .findAny().orElse(null);
        // 保存数据
        assert activityInstance != null;
        deployService.saveData(instance.getId(), definition.getDeploymentId(),
                activityInstance.getActivityId(), variables);
    }


    /**
     * 删除流程实例
     *
     * @param instanceId 流程实例id
     */
    @Override
    public void delete(String instanceId,String remark) {
        // 查询历史数据
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
        if (historicProcessInstance.getEndTime() != null) {
            // 流程已经完成，禁止删除!
            throw new CustomException("流程已经完成，禁止删除!");
//            historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
//            return;
        }
        // 删除流程实例
        runtimeService.deleteProcessInstance(instanceId, remark);
        // 删除历史流程实例
//        historyService.deleteHistoricProcessInstance(instanceId);
    }

    @Override
    public void checkProcess(StartProcessDto dto, String s) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(dto.getDefinitionId()).singleResult();
        if (definition.isSuspended()) throw new CustomException("流程已挂起,不能启动!");
        if (definition.getKey().contains("system"))throw new CustomException("该流程为系统流程需要单独建立启动接口");
    }
}
