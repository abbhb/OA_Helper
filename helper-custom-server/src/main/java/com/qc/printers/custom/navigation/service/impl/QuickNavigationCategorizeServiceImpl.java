package com.qc.printers.custom.navigation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.selectOptionsResult;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationCategorize;
import com.qc.printers.common.navigation.service.IQuickNavigationCategorizeService;
import com.qc.printers.common.navigation.service.IQuickNavigationItemService;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationCategorizeResult;
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

    @Override
    public R<String> updataForQuickNavigationCategorize(QuickNavigationCategorize quickNavigation) {
        if (StringUtils.isEmpty(quickNavigation.getName())){
            return R.error("更新失败");
        }
        if (quickNavigation.getId()==null){
            return R.error("更新失败");
        }
        LambdaUpdateWrapper<QuickNavigationCategorize> quickNavigationCategorizeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        quickNavigationCategorizeLambdaUpdateWrapper.set(QuickNavigationCategorize::getName,quickNavigation.getName());
        quickNavigationCategorizeLambdaUpdateWrapper.eq(QuickNavigationCategorize::getId,quickNavigation.getId());
        boolean update = iQuickNavigationCategorizeService.update(quickNavigationCategorizeLambdaUpdateWrapper);
        if (update){
            return R.success("更新成功");
        }
        return R.error("更新失败");
    }

    /**
     * 需要判断是否有item绑定该分类
     * @param id
     * @return
     */
    @Override
    public R<String> deleteNavigationCategorize(String id) {
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        if (id.contains(",")){
            String[] split = id.split(",");
            for (String s:
                    split) {

                if (iQuickNavigationItemService.hasId(Long.valueOf(s))) {
                    return R.error("该分类绑定了item,请先删除这些item");
                }
                LambdaQueryWrapper<QuickNavigationCategorize> lambdaUpdateWrapper = new LambdaQueryWrapper<>();
                lambdaUpdateWrapper.eq(QuickNavigationCategorize::getId, Long.valueOf(s));
                iQuickNavigationCategorizeService.remove(lambdaUpdateWrapper);
            }
        }else {

            if (iQuickNavigationItemService.hasId(Long.valueOf(id))) {
                return R.error("该分类绑定了item,请先删除这些item");
            }
            LambdaQueryWrapper<QuickNavigationCategorize> lambdaUpdateWrapper = new LambdaQueryWrapper<>();
            lambdaUpdateWrapper.eq(QuickNavigationCategorize::getId, Long.valueOf(id));
            iQuickNavigationCategorizeService.remove(lambdaUpdateWrapper);
        }

        return R.success("删除成功");
    }

    @Override
    public R<List<selectOptionsResult>> getCategorizeSelectOptionsList() {
        List<QuickNavigationCategorize> list = iQuickNavigationCategorizeService.list();
        List<selectOptionsResult> selectOptionsResults = new ArrayList<>();
        for (QuickNavigationCategorize q:
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
    public R<String> createNavCategorize(QuickNavigationCategorize quickNavigationCategorize) {
        if (StringUtils.isEmpty(quickNavigationCategorize.getName())){
            throw new CustomException("必参缺失");
        }
        quickNavigationCategorize.setId(null);
        quickNavigationCategorize.setIsDeleted(null);
        boolean save = iQuickNavigationCategorizeService.save(quickNavigationCategorize);
        if (save){
            return R.success("成功");
        }
        return R.error("失败");
    }


}
