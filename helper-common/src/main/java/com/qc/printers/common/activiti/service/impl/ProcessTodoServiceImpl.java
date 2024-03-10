package com.qc.printers.common.activiti.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.activiti.entity.SysDeployNodeEntity;
import com.qc.printers.common.activiti.entity.dto.workflow.TodoCompleteDto;
import com.qc.printers.common.activiti.entity.dto.workflow.TodoListDto;
import com.qc.printers.common.activiti.entity.vo.workflow.TodoListVo;
import com.qc.printers.common.activiti.service.ProcessTodoService;
import com.qc.printers.common.activiti.service.SysDeployNodeService;
import com.qc.printers.common.activiti.service.SysDeployService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import de.odysseus.el.tree.TreeBuilderException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代办任务
 *
 * @author liuguofeng
 * @date 2023/11/04 12:09
 **/
@Service("processTodoService")
public class ProcessTodoServiceImpl implements ProcessTodoService {


    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SysDeployNodeService deployNodeService;

    @Autowired
    private SysDeployService deployService;

    /**
     * 查看我代办的流程
     *
     * @param dto 参数
     */
    @Override
    public PageData queryPage(TodoListDto dto) {
        List<String> usersGroups = new ArrayList<>();
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
//        usersGroups.add(String.valueOf(currentUser.getDeptId()));
        TaskQuery query = taskService.createTaskQuery()
                .active()
                .taskCandidateOrAssigned(String.valueOf(currentUser.getId()), usersGroups)
                .processDefinitionNameLike("%" + dto.getDefinitionName() + "%")
                .processDefinitionKeyLike("%" + dto.getDefinitionKey() + "%")
                .orderByTaskCreateTime()
                .desc();
        List<Task> list = query
                .listPage(dto.getPageNum() - 1, dto.getPageSize());

        List<TodoListVo> resultList = new ArrayList<>();
        for (Task task : list) {
            TodoListVo vo = new TodoListVo();
            // 当前流程
            vo.setTaskId(task.getId());
            vo.setTaskName(task.getName());
            vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
            vo.setProcessInstanceId(task.getProcessInstanceId());
            vo.setCreateTime(task.getCreateTime());
            vo.setProcessDefinitionId(task.getProcessDefinitionId());

            // 流程定义
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            vo.setDefinitionName(definition.getName());
            vo.setDefinitionKey(definition.getKey());
            vo.setDefinitionVersion(definition.getVersion());

            // 流程发起人
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            User user = userDao.getById(Long.valueOf(instance.getStartUserId()));
            vo.setStartUserId(String.valueOf(user.getId()));
            vo.setStartUserName(user.getUsername());
            resultList.add(vo);
        }
        PageData pageData = new PageData();
        pageData.setTotal(query.count());
        pageData.setRecords(resultList);
        return pageData;
    }

    /**
     * 获取节点表单
     *
     * @param taskId 任务id
     * @return 表单数据
     */
    @Override
    public Map<String, Object> getNodeForm(String taskId) {
        // 获取任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new CustomException("未知任务！");
        // 流程定义信息
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        // 获取当前节点 formKey
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        // 获取表单数据
        SysDeployNodeEntity form = deployNodeService.getOne(new LambdaQueryWrapper<SysDeployNodeEntity>()
                .eq(SysDeployNodeEntity::getDeployId, processDefinition.getDeploymentId())
                .eq(SysDeployNodeEntity::getActivityId, flowElement.getId()));
        // 用户没有填写表单
        if (form == null) {
            return new HashMap<>();
        }
        return form.getFormJson();
    }

    /**
     * 节点审批
     *
     * @param dto 参数
     */
    @Transactional
    @Override
    public void complete(TodoCompleteDto dto) {
        try {
            List<String> usersGroups = new ArrayList<>();
            usersGroups.add(dto.getDeptId());
            Task task = taskService.createTaskQuery()
                    .active()
                    .processInstanceId(dto.getProcessInstanceId())
                    .taskCandidateOrAssigned(dto.getUserId(), usersGroups)
                    .orderByTaskCreateTime()
                    .desc().singleResult();
            if (task == null) throw new CustomException("未找到审批节点!");
            // 如果没有代理人就拾取任务进行办理
            if (StringUtils.isEmpty(task.getAssignee())) {
                taskService.claim(task.getId(), dto.getUserId());
            }

            Map<String, Object> variables = dto.getVariables();
            taskService.setVariables(task.getId(), variables);
            taskService.setVariablesLocal(task.getId(), variables);
            taskService.complete(task.getId());

            // 获取相关数据
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId()).singleResult();
            HistoricActivityInstance activityInstance = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).list()
                    .stream().filter(t -> t.getTaskId() != null && t.getTaskId().equals(task.getId()))
                    .findAny().orElse(null);
            // 保存数据
            assert activityInstance != null;
            deployService.saveData(task.getProcessInstanceId(), definition.getDeploymentId(),
                    activityInstance.getActivityId(), variables);
        } catch (TreeBuilderException ex) {
            throw new CustomException("流程条件表达式错误:" + ex.getMessage());
        } catch (ActivitiException ex) {
            throw new CustomException("Activiti流程异常:" + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CustomException("流程提交未知异常!");
        }
    }
}
