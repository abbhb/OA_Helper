package com.qc.printers.common.activiti.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.activiti.entity.DeployGroup;
import com.qc.printers.common.activiti.mapper.DeployGroupMapper;
import org.springframework.stereotype.Service;

@Service
public class DeployGroupDao extends ServiceImpl<DeployGroupMapper, DeployGroup> {
}
