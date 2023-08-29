package com.qc.printers.common.navigation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationCategorize;
import com.qc.printers.common.navigation.mapper.QuickNavigationCategorizeMapper;
import com.qc.printers.common.navigation.service.IQuickNavigationCategorizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IQuickNavigationCategorizeServiceImpl extends ServiceImpl<QuickNavigationCategorizeMapper, QuickNavigationCategorize> implements IQuickNavigationCategorizeService {
}
