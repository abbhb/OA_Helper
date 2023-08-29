package com.qc.printers.common.navigation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationItem;

public interface IQuickNavigationItemService extends IService<QuickNavigationItem> {
    boolean hasId(Long valueOf);
}
