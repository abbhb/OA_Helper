package com.qc.printers.custom.navigation.service;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.selectOptionsResult;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationCategorizeResult;
import com.qc.printers.custom.navigation.domain.vo.req.QuickNavigationCategorizeUpdateReq;

import java.util.List;

public interface QuickNavigationCategorizeService {
    R<PageData<QuickNavigationCategorizeResult>> listNavFenLei(Integer pageNum, Integer pageSize, String name);

    R<String> updataForQuickNavigationCategorize(QuickNavigationCategorizeUpdateReq quickNavigation);

    R<String> deleteNavigationCategorize(String id);


    R<List<selectOptionsResult>> getCategorizeSelectOptionsList();

    R<String> createNavCategorize(QuickNavigationCategorizeUpdateReq quickNavigationCategorize);
}
