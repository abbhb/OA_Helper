package com.qc.printers.common.activiti.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.activiti.entity.SysDeployEntity;
import com.qc.printers.common.activiti.entity.SysDeployNodeEntity;
import com.qc.printers.common.activiti.entity.TableColumns;
import com.qc.printers.common.activiti.entity.dto.workflow.DefinitionListDto;
import com.qc.printers.common.activiti.entity.dto.workflow.DeployProcessDto;
import com.qc.printers.common.activiti.entity.dto.workflow.FormJsonsDto;
import com.qc.printers.common.activiti.entity.dto.workflow.TableInfoDto;
import com.qc.printers.common.activiti.entity.vo.workflow.DefinitionListVo;
import com.qc.printers.common.activiti.entity.vo.workflow.NodeColumnsVo;
import com.qc.printers.common.activiti.entity.vo.workflow.TableColumnsVo;
import com.qc.printers.common.activiti.entity.vo.workflow.TableInfoVo;
import com.qc.printers.common.activiti.service.ProcessDefinitionService;
import com.qc.printers.common.activiti.service.SysDeployNodeService;
import com.qc.printers.common.activiti.service.SysDeployService;
import com.qc.printers.common.activiti.service.TableService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.entity.PageData;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 流程定义实现
 **/
@Service("processDefinitionService")
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SysDeployNodeService deployNodeService;

    @Autowired
    private SysDeployService deployService;

    @Autowired
    private TableService tableService;

    /**
     * 流程管理列表
     *
     * @param dto 参数
     * @return 列表
     */
    @Override
    public PageData queryPage(DefinitionListDto dto) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionId()
                .orderByProcessDefinitionKey().desc()
                .orderByProcessDefinitionVersion().desc();
        query.processDefinitionNameLike("%" + dto.getDefinitionName() + "%");
        query.processDefinitionKeyLike("%" + dto.getDefinitionKey() + "%");
        if (dto.isActive()) query.active();
        List<ProcessDefinition> list =
                query.listPage(dto.getPageNum() - 1, dto.getPageSize());
        List<DefinitionListVo> resultList = new ArrayList<>();
        for (ProcessDefinition item : list) {
            DefinitionListVo vo = new DefinitionListVo();
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(item.getDeploymentId())
                    .singleResult();
            vo.setDeploymentTime(deployment.getDeploymentTime());
            BeanUtils.copyProperties(item, vo);
            // 获取主表单
            SysDeployNodeEntity mainForm = deployNodeService.getOne(new LambdaQueryWrapper<SysDeployNodeEntity>()
                    .eq(SysDeployNodeEntity::getDeployId, item.getDeploymentId())
                    .eq(SysDeployNodeEntity::getIsMainFrom, 1));
            if (mainForm != null) vo.setFormJson(mainForm.getFormJson());
            resultList.add(vo);
        }
        PageData pageData = new PageData<>();
        pageData.setTotal(query.count());
        pageData.setRecords(resultList);
        return pageData;
    }

    /**
     * 获取流程定义xml
     *
     * @param deploymentId 部署id
     * @return 流程xml字符串
     */
    @Override
    public String getDefinitionXml(String deploymentId) {
        InputStream is = null;
        try {
            is = repositoryService.getResourceAsStream(deploymentId, "index.bpmn");
            byte[] bytes = new byte[is.available()];
            while (is.read(bytes) != -1) ;
            return new String(bytes);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CustomException("获取流程图失败");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 部署流程
     *
     * @param dto 参数
     */
    @Transactional
    @Override
    public void deployProcess(DeployProcessDto dto) {
        // 部署xml
        Deployment deploy = repositoryService.createDeployment().disableBpmnValidation()
                .addString("index.bpmn", dto.getXml())
                .deploy();

        Date date = new Date();
        // 流程部署详情
        TableInfoDto tableInfo = dto.getTableInfo();
        if (tableInfo != null) {
            // 创建表
            tableService.createTable(tableInfo);

            // 部署表信息
            SysDeployEntity sysDeploy = new SysDeployEntity();
            sysDeploy.setTableName(tableInfo.getTableName());
            sysDeploy.setTableComment(tableInfo.getTableComment());
            sysDeploy.setDeployId(deploy.getId());
            sysDeploy.setCreateTime(date);
            deployService.save(sysDeploy);
        }

        // 保存节点数据
        List<FormJsonsDto> formJsons = dto.getFormJsonList();
        List<SysDeployNodeEntity> list = new ArrayList<>();
        for (FormJsonsDto formJson : formJsons) {
            SysDeployNodeEntity deployNode = new SysDeployNodeEntity() {{
                setDeployId(deploy.getId());
                setActivityId(formJson.getActivityId());
                setFormJson(formJson.getFormJson());
                setIsMainFrom(formJson.getIsMainFrom());
                setCreateTime(date);
            }};
            dto.getNodeColumns().stream().filter(t -> t.getActivityId()
                            .equals(formJson.getActivityId())).findAny()
                    .ifPresent(t -> deployNode.setColumns(t.getColumns()));
            list.add(deployNode);
        }
        deployNodeService.saveBatch(list);
    }

    /**
     * 获取流程定义详情
     *
     * @param deploymentId 部署id
     * @return 流程xml字符串和流程表单
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getDefinitionInfo(String deploymentId) {
        // 获取xml
        Map<String, Object> result = new HashMap<>();
        String xml = getDefinitionXml(deploymentId);
        result.put("xml", xml);
        // 获取节点表单
        List<SysDeployNodeEntity> list = deployNodeService.list(new LambdaQueryWrapper<SysDeployNodeEntity>()
                .select(SysDeployNodeEntity::getActivityId,
                        SysDeployNodeEntity::getFormJson,
                        SysDeployNodeEntity::getColumns,
                        SysDeployNodeEntity::getIsMainFrom)
                .eq(SysDeployNodeEntity::getDeployId, deploymentId));

        // 获取节点动态表单
        List<FormJsonsDto> formJsonList = new ArrayList<>();
        for (SysDeployNodeEntity deployNode : list) {
            FormJsonsDto formJsonsDto = new FormJsonsDto();
            BeanUtils.copyProperties(deployNode, formJsonsDto);
            formJsonList.add(formJsonsDto);
        }
        result.put("formJsonList", formJsonList);

        SysDeployEntity sysDeploy = deployService.getById(deploymentId);
        if (sysDeploy != null) {
            // 表结构信息
            TableInfoVo tableInfo = new TableInfoVo();
            tableInfo.setTableName(sysDeploy.getTableName());
            tableInfo.setTableComment(sysDeploy.getTableComment());
            List<TableColumns> tableColumns = tableService.tableColumns(sysDeploy.getTableName(), null);
            List<TableColumnsVo> columns = new ArrayList<>();
            for (TableColumns tableColumn : tableColumns) {
                TableColumnsVo tableColumnsVo = new TableColumnsVo();
                BeanUtils.copyProperties(tableColumn, tableColumnsVo);
                columns.add(tableColumnsVo);
            }
            tableInfo.setColumns(columns);
            result.put("tableInfo", tableInfo);

            // 获取节点绑定数据库表字段的数据
            List<NodeColumnsVo> nodeColumnsVos = new ArrayList<>();
            for (SysDeployNodeEntity deployNode : list) {
                if (deployNode.getColumns() == null) continue;
                NodeColumnsVo nodeColumnsVo = new NodeColumnsVo();
                nodeColumnsVo.setActivityId(deployNode.getActivityId());
                nodeColumnsVo.setColumns(deployNode.getColumns());
                nodeColumnsVos.add(nodeColumnsVo);
            }
            result.put("nodeColumns", nodeColumnsVos);
        }
        return result;
    }

    /**
     * 更新流程定义状态 激活或者挂起
     *
     * @param deploymentId 部署id
     */
    @Override
    public void updateState(String deploymentId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .singleResult();
        if (definition == null) throw new CustomException("未知流程");
        boolean isSuspended = repositoryService.isProcessDefinitionSuspended(definition.getId());
        if (isSuspended) {
            repositoryService.activateProcessDefinitionById(definition.getId());
        } else {
            repositoryService.suspendProcessDefinitionById(definition.getId());
        }
    }

    /**
     * 删除流程
     *
     * @param deploymentId 部署id
     */
    @Transactional
    @Override
    public void delete(String deploymentId) {
        deployNodeService.remove(new LambdaQueryWrapper<SysDeployNodeEntity>()
                .eq(SysDeployNodeEntity::getDeployId, deploymentId));
        deployService.remove(new LambdaQueryWrapper<SysDeployEntity>()
                .eq(SysDeployEntity::getDeployId, deploymentId));
        repositoryService.deleteDeployment(deploymentId, true);
    }
}
