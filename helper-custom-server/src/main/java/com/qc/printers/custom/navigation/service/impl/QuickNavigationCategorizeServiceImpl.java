package com.qc.printers.custom.navigation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.selectOptionsResult;
import com.qc.printers.common.navigation.dao.QuickNavigationDeptDao;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationCategorize;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationDept;
import com.qc.printers.common.navigation.service.IQuickNavigationCategorizeService;
import com.qc.printers.common.navigation.service.IQuickNavigationItemService;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationCategorizeResult;
import com.qc.printers.custom.navigation.domain.vo.req.QuickNavigationCategorizeUpdateReq;
import com.qc.printers.custom.navigation.service.QuickNavigationCategorizeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class QuickNavigationCategorizeServiceImpl implements QuickNavigationCategorizeService {

    @Autowired
    private IQuickNavigationCategorizeService iQuickNavigationCategorizeService;
    @Autowired
    private IQuickNavigationItemService iQuickNavigationItemService;
    @Autowired
    private QuickNavigationDeptDao quickNavigationDeptDao;
    @Autowired
    private ISysDeptService iSysDeptService;


    /**
     * 后期通过注解controller校验权限
     *
     * @param name
     * @return
     */
    @Override
    public R<PageData<QuickNavigationCategorizeResult>> listNavFenLei(Integer pageNum, Integer pageSize, String name) {
        if (pageNum == null) {
            return R.error("传参错误");
        }
        if (pageSize==null){
            return R.error("传参错误");
        }
        Page pageInfo = new Page(pageNum,pageSize);
        LambdaQueryWrapper<QuickNavigationCategorize> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(!StringUtils.isEmpty(name),QuickNavigationCategorize::getName,name);

        iQuickNavigationCategorizeService.page(pageInfo, lambdaQueryWrapper);


        PageData<QuickNavigationCategorizeResult> pageData = new PageData<>();
        List<QuickNavigationCategorizeResult> results = new ArrayList<>();
        for (Object quickNavigationCategorize : pageInfo.getRecords()) {
            QuickNavigationCategorize quickNavigationCategorize1 = (QuickNavigationCategorize) quickNavigationCategorize;

            QuickNavigationCategorizeResult quickNavigationCategorizeResult = new QuickNavigationCategorizeResult();
            quickNavigationCategorizeResult.setName(quickNavigationCategorize1.getName());
            quickNavigationCategorizeResult.setId(String.valueOf(quickNavigationCategorize1.getId()));
            quickNavigationCategorizeResult.setVisibility(quickNavigationCategorize1.getVisibility());
            if (quickNavigationCategorize1.getVisibility().equals(1)) {
                // 当仅部门可见
                LambdaQueryWrapper<QuickNavigationDept> sysQuickNavigationDept = new LambdaQueryWrapper<>();
                sysQuickNavigationDept.eq(QuickNavigationDept::getQuickNavCategorizeId, quickNavigationCategorize1.getId());
                sysQuickNavigationDept.select(QuickNavigationDept::getDeptId);
                List<Long> deptList = quickNavigationDeptDao.list(sysQuickNavigationDept).stream().map(quickNavigationDept -> quickNavigationDept.getDeptId()).toList();
                LambdaQueryWrapper<SysDept> sysDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
                if (deptList.size() > 0) {
                    // 当绑定了部门
                    sysDeptLambdaQueryWrapper.in(SysDept::getId, deptList);
                    sysDeptLambdaQueryWrapper.select(SysDept::getId, SysDept::getDeptName);
                    List<SysDept> sysDeptList = iSysDeptService.list(sysDeptLambdaQueryWrapper);
                    quickNavigationCategorizeResult.setDepts(sysDeptList);
                }
            }


            results.add(quickNavigationCategorizeResult);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return R.success(pageData);

    }

    @Transactional
    @Override
    public R<String> updataForQuickNavigationCategorize(QuickNavigationCategorizeUpdateReq quickNavigation) {
        if (StringUtils.isEmpty(quickNavigation.getQuickNavigationCategorize().getName())) {
            return R.error("更新失败");
        }
        if (quickNavigation.getQuickNavigationCategorize().getId() == null) {
            return R.error("更新失败");
        }
        QuickNavigationCategorize quickNavigationCategorize = iQuickNavigationCategorizeService.getById(quickNavigation.getQuickNavigationCategorize().getId());
        quickNavigationCategorize.setName(quickNavigation.getQuickNavigationCategorize().getName());
        quickNavigationCategorize.setVisibility(quickNavigation.getQuickNavigationCategorize().getVisibility());
        boolean update = iQuickNavigationCategorizeService.updateById(quickNavigationCategorize);

        LambdaQueryWrapper<QuickNavigationDept> quickNavigationDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        quickNavigationDeptLambdaQueryWrapper.eq(QuickNavigationDept::getQuickNavCategorizeId, quickNavigationCategorize.getId());

        if (quickNavigation.getQuickNavigationCategorize().getVisibility().equals(1)) {
            // 仅部门可见
            int count = quickNavigationDeptDao.count(quickNavigationDeptLambdaQueryWrapper);
            if (count > 0) {
                // 先删除当前分类的绑定
                quickNavigationDeptDao.remove(quickNavigationDeptLambdaQueryWrapper);
            }
            // 添加当前绑定
            for (int i = 0; i < quickNavigation.getVisDeptIds().size(); i++) {
                QuickNavigationDept quickNavigationDept = new QuickNavigationDept();
                quickNavigationDept.setQuickNavCategorizeId(quickNavigationCategorize.getId());
                quickNavigationDept.setDeptId(quickNavigation.getVisDeptIds().get(i));
                quickNavigationDeptDao.save(quickNavigationDept);
            }
        }
        if (update) {
            return R.success("更新成功");
        }
        return R.error("更新失败");
    }

    /**
     * 需要判断是否有item绑定该分类
     * @param id
     * @return
     */
    @Transactional
    @Override
    public R<String> deleteNavigationCategorize(String id) {
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        if (id.contains(",")){
            String[] split = id.split(",");
            for (String s :
                    split) {
                removeNavigationCategorizeOne(Long.valueOf(id));
            }
        } else {
            removeNavigationCategorizeOne(Long.valueOf(id));
        }
        return R.success("删除成功");
    }

    @Transactional
    public void removeNavigationCategorizeOne(Long id) {
        if (iQuickNavigationItemService.hasId(id)) {
            throw new CustomException("该分类绑定了item,请先删除这些item");
        }
        LambdaQueryWrapper<QuickNavigationCategorize> lambdaUpdateWrapper = new LambdaQueryWrapper<>();
        lambdaUpdateWrapper.eq(QuickNavigationCategorize::getId, Long.valueOf(id));
        iQuickNavigationCategorizeService.remove(lambdaUpdateWrapper);
        LambdaQueryWrapper<QuickNavigationDept> quickNavigationDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        quickNavigationDeptLambdaQueryWrapper.eq(QuickNavigationDept::getQuickNavCategorizeId, id);
        int count = quickNavigationDeptDao.count(quickNavigationDeptLambdaQueryWrapper);
        if (count > 0) {
            // 删除废弃信息
            quickNavigationDeptDao.remove(quickNavigationDeptLambdaQueryWrapper);
        }
    }

    @Override
    public R<List<selectOptionsResult>> getCategorizeSelectOptionsList() {
        List<QuickNavigationCategorize> list = iQuickNavigationCategorizeService.list();
        List<selectOptionsResult> selectOptionsResults = new ArrayList<>();
        for (QuickNavigationCategorize q :
                list) {
            selectOptionsResult selectOptionsResult = new selectOptionsResult();
            selectOptionsResult.setLabel(q.getName());
            selectOptionsResult.setValue(String.valueOf(q.getId()));
            selectOptionsResults.add(selectOptionsResult);
        }
        return R.success(selectOptionsResults);
    }

    @Transactional
    @Override
    public R<String> createNavCategorize(QuickNavigationCategorizeUpdateReq quickNavigationCategorize) {
        if (StringUtils.isEmpty(quickNavigationCategorize.getQuickNavigationCategorize().getName())) {
            throw new CustomException("必参缺失");
        }
        LambdaQueryWrapper<QuickNavigationCategorize> quickNavigationCategorizeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        quickNavigationCategorizeLambdaQueryWrapper.eq(QuickNavigationCategorize::getName, quickNavigationCategorize.getQuickNavigationCategorize().getName());
        int counted = iQuickNavigationCategorizeService.count(quickNavigationCategorizeLambdaQueryWrapper);
        if (counted > 0) {
            throw new CustomException("分类名不能重复");
        }
        QuickNavigationCategorize quickNavigationCategorize1 = quickNavigationCategorize.getQuickNavigationCategorize();
        quickNavigationCategorize1.setId(null);
        quickNavigationCategorize1.setIsDeleted(null);
        boolean save = iQuickNavigationCategorizeService.save(quickNavigationCategorize1);
        LambdaQueryWrapper<QuickNavigationDept> quickNavigationDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        quickNavigationDeptLambdaQueryWrapper.eq(QuickNavigationDept::getQuickNavCategorizeId, quickNavigationCategorize1.getId());
        if (quickNavigationCategorize.getQuickNavigationCategorize().getVisibility().equals(1)) {
            if (quickNavigationCategorize.getVisDeptIds().size() < 1) {
                throw new CustomException("最少绑定一个部门");
            }
            // 仅部门可见
            int count = quickNavigationDeptDao.count(quickNavigationDeptLambdaQueryWrapper);

            if (count > 0) {
                // 先删除当前分类的绑定
                quickNavigationDeptDao.remove(quickNavigationDeptLambdaQueryWrapper);
            }
            // 添加当前绑定
            for (int i = 0; i < quickNavigationCategorize.getVisDeptIds().size(); i++) {
                QuickNavigationDept quickNavigationDept = new QuickNavigationDept();
                quickNavigationDept.setQuickNavCategorizeId(quickNavigationCategorize1.getId());
                quickNavigationDept.setDeptId(quickNavigationCategorize.getVisDeptIds().get(i));
                quickNavigationDeptDao.save(quickNavigationDept);
            }
        }
        if (save) {
            return R.success("成功");
        }
        return R.error("失败");
    }


}
