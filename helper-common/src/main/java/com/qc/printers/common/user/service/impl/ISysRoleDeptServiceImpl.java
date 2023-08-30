package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.user.domain.entity.SysRoleDept;
import com.qc.printers.common.user.mapper.SysRoleDeptMapper;
import com.qc.printers.common.user.service.ISysRoleDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ISysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements ISysRoleDeptService {
}
