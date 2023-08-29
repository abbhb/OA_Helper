package com.qc.printers.custom.navigation.service.impl;


import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.permissionstringsplit.MySplit;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationCategorize;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationItem;
import com.qc.printers.common.navigation.service.IQuickNavigationCategorizeService;
import com.qc.printers.common.navigation.service.IQuickNavigationItemService;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationItemResult;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationResult;
import com.qc.printers.custom.navigation.service.QuickNavigationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuickNavigationServiceImpl implements QuickNavigationService {

    @Autowired
    private IQuickNavigationCategorizeService iQuickNavigationCategorizeService;

    @Autowired
    private IQuickNavigationItemService iQuickNavigationCategorizeItemService;

    @Override
    public R<List<QuickNavigationResult>> list(Long userId) {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            return R.error("异常1");
        }
        if (userId == null) {
            return R.error("异常");
        }
        List<QuickNavigationCategorize> quickNavigationCategorizes = iQuickNavigationCategorizeService.list();
        List<QuickNavigationItem> quickNavigationItems = iQuickNavigationCategorizeItemService.list();
        List<QuickNavigationResult> quickNavigationResults = new ArrayList<>();


        for (QuickNavigationCategorize quickNavigationCategorize :
                quickNavigationCategorizes) {
            QuickNavigationResult quickNavigationResult = new QuickNavigationResult();
            quickNavigationResult.setId(String.valueOf(quickNavigationCategorize.getId()));
            quickNavigationResult.setName(quickNavigationCategorize.getName());
            List<QuickNavigationItem> result1 = quickNavigationItems.stream()
                    .filter(item -> item.getCategorizeId().equals(quickNavigationCategorize.getId()))
                    .collect(Collectors.toList());

            List<QuickNavigationItemResult> quickNavigationItemResults = new ArrayList<>();
            for (QuickNavigationItem quickNavigationItem:
                    result1) {
                QuickNavigationItemResult quickNavigationItemResult = new QuickNavigationItemResult();
                quickNavigationItemResult.setId(String.valueOf(quickNavigationItem.getId()));
                quickNavigationItemResult.setName(quickNavigationItem.getName());
                quickNavigationItemResult.setPath(quickNavigationItem.getPath());
                quickNavigationItemResult.setImage(quickNavigationItem.getImage());
                quickNavigationItemResult.setIntroduction(quickNavigationItem.getIntroduction());
                quickNavigationItemResult.setType(quickNavigationItem.getType());
                //markdown
                quickNavigationItemResult.setContent(MySplit.splitString(quickNavigationItem.getContent(),currentUser.getPermission()));
                quickNavigationItemResult.setCategorizeId(String.valueOf(quickNavigationItem.getCategorizeId()));
                QuickNavigationCategorize quickNavigationCategorizeServiceById = iQuickNavigationCategorizeService.getById(quickNavigationItem.getCategorizeId());
                if (quickNavigationCategorizeServiceById==null){
                    throw new CustomException("运行异常");
                }
                quickNavigationItemResult.setCategorizeName(quickNavigationCategorizeServiceById.getName());
                String permission = quickNavigationItem.getPermission();
                if (permission.contains(",")){
                    List<Integer> integerList = new ArrayList<>();
                    String[] split = permission.split(",");
                    for (String s:
                         split) {
                        integerList.add(Integer.valueOf(s));
                    }
                    quickNavigationItemResult.setPermission(integerList);
                }else {
                    List<Integer> integerList = new ArrayList<>();
                    integerList.add(Integer.valueOf(permission));
                    quickNavigationItemResult.setPermission(integerList);
                }
                quickNavigationItemResults.add(quickNavigationItemResult);
            }
            quickNavigationResult.setItem(quickNavigationItemResults);
            quickNavigationResults.add(quickNavigationResult);
        }
        return R.success(quickNavigationResults);
    }
}
