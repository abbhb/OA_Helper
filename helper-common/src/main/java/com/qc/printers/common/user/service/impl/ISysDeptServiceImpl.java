package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.annotation.DataScope;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.mapper.SysDeptMapper;
import com.qc.printers.common.user.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ISysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
    @DataScope(deptAlias = "sys_dept")
    @Override
    public List<SysDept> listSysDeptWithScope(SysDept sysDept) {
        LambdaQueryWrapper<SysDept> sysDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysDeptLambdaQueryWrapper.apply(StringUtils.isNotEmpty(sysDept.getExistSql()),sysDept.getExistSql());
        return this.list(sysDeptLambdaQueryWrapper);
    }
}
