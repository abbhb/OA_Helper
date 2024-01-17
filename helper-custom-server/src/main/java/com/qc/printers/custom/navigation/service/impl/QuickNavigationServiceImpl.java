package com.qc.printers.custom.navigation.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.permissionstringsplit.MySplit;
import com.qc.printers.common.navigation.dao.QuickNavigationDeptDao;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationCategorize;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationDept;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationItem;
import com.qc.printers.common.navigation.service.IQuickNavigationCategorizeService;
import com.qc.printers.common.navigation.service.IQuickNavigationItemService;
import com.qc.printers.common.user.domain.dto.UserInfo;
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

    @Autowired
    private QuickNavigationDeptDao quickNavigationDeptDao;

    /**
     * 只返回有权限看见的
     *
     * @return
     */
    @Override
    public R<List<QuickNavigationResult>> list() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            return R.error("异常1");
        }
        // 当前用户所属部门id
        Long currentUserDeptId = currentUser.getDeptId();
        //todo：先查出可见性为0的部门，再找为1的，而且筛选出当前部门id在允许范围的，综合为最终返回结果
        LambdaQueryWrapper<QuickNavigationDept> quickNavigationDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        quickNavigationDeptLambdaQueryWrapper.eq(QuickNavigationDept::getDeptId, currentUserDeptId);
        quickNavigationDeptLambdaQueryWrapper.select(QuickNavigationDept::getQuickNavCategorizeId);
        // 找到该部门被授权的分类id列表
        List<Long> quickNavigationDeptList = quickNavigationDeptDao.list(quickNavigationDeptLambdaQueryWrapper).stream().map(QuickNavigationDept::getQuickNavCategorizeId).toList();
        LambdaQueryWrapper<QuickNavigationCategorize> quickNavigationCategorizeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        quickNavigationCategorizeLambdaQueryWrapper.eq(QuickNavigationCategorize::getVisibility, 0);
        if (quickNavigationDeptList != null && quickNavigationDeptList.size() > 0) {
            quickNavigationCategorizeLambdaQueryWrapper.or().in(QuickNavigationCategorize::getId, quickNavigationDeptList);
        }
        // 找到该部门被授权的分类id列表对应的分类实体类列表
        List<QuickNavigationCategorize> quickNavigationCategorizes = iQuickNavigationCategorizeService.list(quickNavigationCategorizeLambdaQueryWrapper);
        List<Long> categorizeIds = quickNavigationCategorizes.stream().map(QuickNavigationCategorize::getId).toList();
        LambdaQueryWrapper<QuickNavigationItem> quickNavigationItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (categorizeIds == null) {
            throw new CustomException("为空");
        }
        if (categorizeIds.size() == 0) {
            throw new CustomException("为空");
        }
        quickNavigationItemLambdaQueryWrapper.in(QuickNavigationItem::getCategorizeId, categorizeIds);
        // 找到这些分类实体类列表id所对应的item实体类列表
        List<QuickNavigationItem> quickNavigationItems = iQuickNavigationCategorizeItemService.list(quickNavigationItemLambdaQueryWrapper);
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
                quickNavigationItemResult.setContent(MySplit.splitString(quickNavigationItem.getContent(), currentUser.getSysRoles()));
                quickNavigationItemResult.setCategorizeId(String.valueOf(quickNavigationItem.getCategorizeId()));
                QuickNavigationCategorize quickNavigationCategorizeServiceById = iQuickNavigationCategorizeService.getById(quickNavigationItem.getCategorizeId());
                if (quickNavigationCategorizeServiceById==null){
                    throw new CustomException("运行异常");
                }
                quickNavigationItemResult.setCategorizeName(quickNavigationCategorizeServiceById.getName());
                quickNavigationItemResults.add(quickNavigationItemResult);
            }
            quickNavigationResult.setItem(quickNavigationItemResults);
            quickNavigationResults.add(quickNavigationResult);
        }
        return R.success(quickNavigationResults);
    }
}
