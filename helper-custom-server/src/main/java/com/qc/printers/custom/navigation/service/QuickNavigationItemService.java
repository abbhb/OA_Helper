package com.qc.printers.custom.navigation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationItem;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationItemResult;


public interface QuickNavigationItemService {

    R<PageData<QuickNavigationItemResult>> listNavFenLeiItem(Integer pageNum, Integer pageSize, String name, String selectCate);

    R<String> createNavItem(QuickNavigationItem quickNavigationItem);

    R<String> deleteNavigationItem(String id);

    R<String> updataForQuickNavigationItem(QuickNavigationItem quickNavigationItem);
}
