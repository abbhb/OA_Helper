package com.qc.printers.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.user.domain.entity.SysDept;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {
    List<SysDept> listSysDeptWithScope(SysDept sysDept);
}
