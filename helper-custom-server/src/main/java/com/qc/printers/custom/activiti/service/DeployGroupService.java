package com.qc.printers.custom.activiti.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.activiti.dao.DeployExtDao;
import com.qc.printers.common.activiti.dao.DeployGroupActDao;
import com.qc.printers.common.activiti.dao.DeployGroupDao;
import com.qc.printers.common.activiti.entity.DeployExt;
import com.qc.printers.common.activiti.entity.DeployGroup;
import com.qc.printers.common.activiti.entity.DeployGroupAct;
import com.qc.printers.common.activiti.entity.SysDeployNodeEntity;
import com.qc.printers.common.activiti.entity.vo.workflow.DefinitionListVo;
import com.qc.printers.common.activiti.service.SysDeployNodeService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.custom.activiti.pojo.dto.DeployGroupActDto;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DeployGroupService {
    @Autowired
    private DeployGroupDao deployGroupDao;


    //    关联表
    @Autowired
    private DeployGroupActDao deployGroupActDao;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SysDeployNodeService deployNodeService;


    @Autowired
    private DeployExtDao deployExtDao;

    public List<DeployGroup> getDeployGroupList() {
        List<DeployGroup> list = deployGroupDao.list();
        DeployGroup deployGroup = new DeployGroup();
        deployGroup.setSort(0);
        deployGroup.setName("未分组");
        deployGroup.setId(0L);
        list.add(deployGroup);
        Collections.sort(list, new Comparator<DeployGroup>() {

            @Override
            public int compare(DeployGroup deployGroup1, DeployGroup deployGroup2) {
                // TODO Auto-generated method stub

                return deployGroup1.getSort().compareTo(deployGroup2.getSort());
            }

        });
        return list;
    }

    @Transactional
    public void addDeployGroup(DeployGroup deployGroup) {
        deployGroup.setId(null);
        deployGroup.setIsDeleted(0);
        if (deployGroup.getSort() == null || deployGroup.getSort() < 1) {
            deployGroup.setSort(1);
        }
        deployGroupDao.save(deployGroup);
    }

    @Transactional
    public void putDeployGroup(DeployGroup deployGroup) {
        if (deployGroup.getId() == null) {
            throw new CustomException("id不能为空");
        }
        if (StringUtils.isEmpty(deployGroup.getName())) {
            throw new CustomException("Name不能为空");
        }
        if (deployGroup.getSort() == null || deployGroup.getSort() < 1) {
            deployGroup.setSort(1);
        }
        if (deployGroup.getId() == 0L) {
            throw new CustomException("未分组禁止编辑");
        }

        deployGroupDao.updateById(deployGroup);
    }

    @Transactional
    public void deleteDeployGroup(Long deployGroupId) {
        if (deployGroupId == null) {
            throw new CustomException("id不能为空");
        }
        if (deployGroupId == 0L) {
            throw new CustomException("未分组禁止编辑");
        }

        LambdaQueryWrapper<DeployGroupAct> deployGroupActLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deployGroupActLambdaQueryWrapper.eq(DeployGroupAct::getDeployGroupId, deployGroupId);
        int count = (int) deployGroupActDao.count(deployGroupActLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("该组存在关联流程，请先手动解除");
        }
        deployGroupDao.removeById(deployGroupId);
    }

    public List<DeployGroupActDto> getDeployGroupActList() {
        List<DeployGroupActDto> deployGroupActDtos = new ArrayList<>();
        DeployGroupActDto deployGroupActDto = new DeployGroupActDto();
        deployGroupActDto.setDeployGroupId(0L);
        deployGroupActDto.setDeployGroupName("未分组");

        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionId()
                .orderByProcessDefinitionKey().desc()
                .orderByProcessDefinitionVersion().desc();
        query.active();
        List<DeployGroup> list1 = deployGroupDao.list();
        HashMap<Long, List<DefinitionListVo>> jieguo = new HashMap<>();
        for (int i = list1.size() - 1; i >= 0; i--) {
            if (jieguo.containsKey(list1.get(i).getId())) {
                continue;
            }
            jieguo.put(list1.get(i).getId(), new ArrayList<>());
        }
//        query.processDefinitionNameLike("%" + dto.getDefinitionName() + "%");
//        query.processDefinitionKeyLike("%" + dto.getDefinitionKey() + "%");
        List<ProcessDefinition> list =
                query.list();
        List<DefinitionListVo> resultListWeiFenZu = new ArrayList<>();
        for (ProcessDefinition item : list) {
            if (item.getKey().contains("system"))continue;// 跳过系统流程，系统流程不允许直接发起
            DefinitionListVo vo = new DefinitionListVo();

            BeanUtils.copyProperties(item, vo);

            DeployExt deployExt = deployExtDao.getById(item.getDeploymentId());
            if (deployExt != null) {
                vo.setIcon(deployExt.getIcon());
            } else {
                vo.setIcon("add");
            }
            LambdaQueryWrapper<DeployGroupAct> deployGroupActLambdaQueryWrapper = new LambdaQueryWrapper<>();
            deployGroupActLambdaQueryWrapper.eq(DeployGroupAct::getDeployId, item.getDeploymentId());
            DeployGroupAct one = deployGroupActDao.getOne(deployGroupActLambdaQueryWrapper);
            if (one != null && jieguo.containsKey(one.getDeployGroupId())) {
                jieguo.get(one.getDeployGroupId()).add(vo);
            } else {
                // 不存在
                resultListWeiFenZu.add(vo);
            }
            // 获取主表单
            SysDeployNodeEntity mainForm = deployNodeService.getOne(new LambdaQueryWrapper<SysDeployNodeEntity>()
                    .eq(SysDeployNodeEntity::getDeployId, item.getDeploymentId())
                    .eq(SysDeployNodeEntity::getIsMainFrom, 1));
            if (mainForm != null) vo.setFormJson(mainForm.getFormJson());
//            resultList.add(vo);
        }
        deployGroupActDto.setDefinitionListVoList(resultListWeiFenZu);
        deployGroupActDto.setSort(0);
        deployGroupActDtos.add(deployGroupActDto);
        for (int i = list1.size() - 1; i >= 0; i--) {
            DeployGroupActDto deployGroupActDtoi = new DeployGroupActDto();
            List<DefinitionListVo> definitionListVos = jieguo.get(list1.get(i).getId());
            deployGroupActDtoi.setDefinitionListVoList(definitionListVos);
            deployGroupActDtoi.setDeployGroupId(list1.get(i).getId());
            deployGroupActDtoi.setDeployGroupName(list1.get(i).getName());
            deployGroupActDtoi.setSort(list1.get(i).getSort());
            deployGroupActDtos.add(deployGroupActDtoi);
        }
        // 排序deployGroupActDtos
//        deployGroupActDtos.sort(c);
        Collections.sort(deployGroupActDtos, new Comparator<DeployGroupActDto>() {

            @Override
            public int compare(DeployGroupActDto deployGroupActDto1, DeployGroupActDto deployGroupActDto2) {
                // TODO Auto-generated method stub

                return deployGroupActDto1.getSort().compareTo(deployGroupActDto2.getSort());
            }

        });

        return deployGroupActDtos;
    }


    @Transactional
    public void addDeployGroupAct(DeployGroupAct deployGroupAct) {
        deployGroupAct.setId(null);
        deployGroupActDao.save(deployGroupAct);
    }


    @Transactional
    public void putDeployGroupAct(DeployGroupAct deployGroupAct) {
        if (deployGroupAct.getDeployGroupId() == null) {
            throw new CustomException("DeployGroupId不能为空");
        }
        if (deployGroupAct.getDeployId() == null) {
            throw new CustomException("DeployId不能为空");
        }
        LambdaQueryWrapper<DeployGroupAct> deployGroupActLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deployGroupActLambdaQueryWrapper.eq(DeployGroupAct::getDeployId, deployGroupAct.getDeployId());
        DeployGroupAct one = deployGroupActDao.getOne(deployGroupActLambdaQueryWrapper);
        if (one != null) {
            deployGroupAct.setId(one.getId());
            deployGroupAct.setDeployId(one.getDeployId());
            deployGroupActDao.updateById(deployGroupAct);
            return;
        }
        deployGroupAct.setId(null);

        this.addDeployGroupAct(deployGroupAct);


    }

    @Transactional
    public void deleteDeployGroupAct(Long deployGroupActId) {
        if (deployGroupActId == null) {
            throw new CustomException("id不能为空");
        }
        deployGroupActDao.removeById(deployGroupActId);
    }
}
