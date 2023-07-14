package com.qc.printers.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.R;
import com.qc.printers.pojo.PageData;
import com.qc.printers.pojo.QuickNavigationItem;
import com.qc.printers.pojo.vo.QuickNavigationItemResult;

public interface QuickNavigationItemService extends IService<QuickNavigationItem> {
    boolean hasId(Long valueOf);

    R<PageData<QuickNavigationItemResult>> listNavFenLeiItem(Integer pageNum, Integer pageSize, String name,String selectCate);

    R<String> createNavItem(QuickNavigationItem quickNavigationItem);

    R<String> deleteNavigationItem(String id);

    R<String> updataForQuickNavigationItem(QuickNavigationItem quickNavigationItem);
}
