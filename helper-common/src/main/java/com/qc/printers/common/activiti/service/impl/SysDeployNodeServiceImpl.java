package com.qc.printers.common.activiti.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.activiti.mapper.SysDeployNodeMapper;
import com.qc.printers.common.activiti.entity.SysDeployNodeEntity;
import com.qc.printers.common.activiti.service.SysDeployNodeService;
import org.springframework.stereotype.Service;


@Service("sysNodeDataService")
public class SysDeployNodeServiceImpl extends ServiceImpl<SysDeployNodeMapper, SysDeployNodeEntity> implements SysDeployNodeService {


}