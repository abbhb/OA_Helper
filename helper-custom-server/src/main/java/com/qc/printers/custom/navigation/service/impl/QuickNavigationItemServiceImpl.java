package com.qc.printers.custom.navigation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationCategorize;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationItem;
import com.qc.printers.common.navigation.mapper.QuickNavigationItemMapper;
import com.qc.printers.common.navigation.service.IQuickNavigationCategorizeService;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationItemResult;
import com.qc.printers.custom.navigation.service.QuickNavigationItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class QuickNavigationItemServiceImpl extends ServiceImpl<QuickNavigationItemMapper, QuickNavigationItem> implements QuickNavigationItemService {

    @Autowired
    private IQuickNavigationCategorizeService iQuickNavigationCategorizeService;


    @Override
    public R<PageData<QuickNavigationItemResult>> listNavFenLeiItem(Integer pageNum, Integer pageSize, String name, String selectCate) {
        if (pageNum == null) {
            return R.error("传参错误");
        }
        if (pageSize == null) {
            return R.error("传参错误");
        }

        Page<QuickNavigationItem> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<QuickNavigationItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //条件过滤
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),QuickNavigationItem::getName,name);
        if (!StringUtils.isEmpty(selectCate)){
            for (String sss:
                 selectCate.split(",")) {
                if (!StringUtils.isEmpty(sss)){
                    lambdaQueryWrapper.or().eq(QuickNavigationItem::getCategorizeId,Long.valueOf(sss));
                }
            }
        }
        super.page(pageInfo,lambdaQueryWrapper);


        PageData<QuickNavigationItemResult> pageData = new PageData<>();
        List<QuickNavigationItemResult> results = new ArrayList<>();
        for (QuickNavigationItem quickNavigationItem : pageInfo.getRecords()) {

            QuickNavigationItemResult quickNavigationItemResult = new QuickNavigationItemResult();
            quickNavigationItemResult.setName(quickNavigationItem.getName());
            quickNavigationItemResult.setId(String.valueOf(quickNavigationItem.getId()));
            quickNavigationItemResult.setIntroduction(quickNavigationItem.getIntroduction());
            quickNavigationItemResult.setPath(quickNavigationItem.getPath());
            quickNavigationItemResult.setImage(OssDBUtil.toUseUrl(quickNavigationItem.getImage()));
            quickNavigationItemResult.setType(quickNavigationItem.getType());
            quickNavigationItemResult.setContent(quickNavigationItem.getContent());
            quickNavigationItemResult.setCategorizeId(String.valueOf(quickNavigationItem.getCategorizeId()));


            /*
            * 后期这种简单粗暴可以优化成map里找
            * */
            QuickNavigationCategorize quickNavigationCategorize = iQuickNavigationCategorizeService.getById(quickNavigationItem.getCategorizeId());
//            log.info("quickNavigationCategorize = {}",quickNavigationCategorize);
            if (quickNavigationCategorize==null){
                throw new CustomException("运行异常");
            }
            quickNavigationItemResult.setCategorizeName(quickNavigationCategorize.getName());
            results.add(quickNavigationItemResult);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
//        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
//        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return R.success(pageData);
    }

    @Transactional
    @Override
    public R<String> createNavItem(QuickNavigationItem quickNavigationItem) {
        if(StringUtils.isEmpty(quickNavigationItem.getName())){
            throw new CustomException("必参缺少");
        }
        if(quickNavigationItem.getCategorizeId()==null){
            throw new CustomException("必参缺少");
        }
        if (quickNavigationItem.getType()==null){
            throw new CustomException("必参缺少");
        }
        if (quickNavigationItem.getType().equals(0)) {
            if(StringUtils.isEmpty(quickNavigationItem.getPath())){
                throw new CustomException("必参缺少");
            }
        }
        if (quickNavigationItem.getType().equals(1)) {
            if(StringUtils.isEmpty(quickNavigationItem.getContent())){
                throw new CustomException("必参缺少");
            }
        }
        boolean save = super.save(quickNavigationItem);
        if (save){
            return R.success("添加成功");
        }
        return R.error("添加失败");
    }

    @Transactional
    @Override
    public R<String> deleteNavigationItem(String id) {
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        Collection<Long> ids = new ArrayList<>();
        if (id.contains(",")){
            String[] split = id.split(",");
            for (String s:
                    split) {

                ids.add(Long.valueOf(s));
            }
            super.removeByIds(ids);
        }else {

            LambdaQueryWrapper<QuickNavigationItem> lambdaUpdateWrapper = new LambdaQueryWrapper<>();
            lambdaUpdateWrapper.eq(QuickNavigationItem::getId,Long.valueOf(id));
            super.remove(lambdaUpdateWrapper);
        }

        return R.success("删除成功");
    }

    @Transactional
    @Override
    public R<String> updataForQuickNavigationItem(QuickNavigationItem quickNavigationItem) {
        if (StringUtils.isEmpty(quickNavigationItem.getName())){
            return R.error("更新失败");
        }
        if (quickNavigationItem.getId()==null){
            return R.error("更新失败");
        }
        if (quickNavigationItem.getCategorizeId()==null){
            return R.error("更新失败");
        }
        if (quickNavigationItem.getType()==null){
            return R.error("更新失败");
        }
        if (StringUtils.isEmpty(quickNavigationItem.getName())){
            return R.error("更新失败");
        }
        if (quickNavigationItem.getType()==null){
            throw new CustomException("必参缺少");
        }
        if (quickNavigationItem.getType().equals(0)) {
            if(StringUtils.isEmpty(quickNavigationItem.getPath())){
                throw new CustomException("必参缺少");
            }
        }
        if (quickNavigationItem.getType().equals(1)) {
            if(StringUtils.isEmpty(quickNavigationItem.getContent())){
                throw new CustomException("必参缺少");
            }
        }
        LambdaUpdateWrapper<QuickNavigationItem> quickNavigationItemLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        quickNavigationItemLambdaUpdateWrapper.set(QuickNavigationItem::getName,quickNavigationItem.getName());
        quickNavigationItemLambdaUpdateWrapper.eq(QuickNavigationItem::getId,quickNavigationItem.getId());
        quickNavigationItemLambdaUpdateWrapper.set(!StringUtils.isEmpty(quickNavigationItem.getImage()),QuickNavigationItem::getImage,OssDBUtil.toDBUrl(quickNavigationItem.getImage()));
        quickNavigationItemLambdaUpdateWrapper.set(QuickNavigationItem::getIntroduction,quickNavigationItem.getIntroduction());
        quickNavigationItemLambdaUpdateWrapper.set(QuickNavigationItem::getPath,quickNavigationItem.getPath());
        quickNavigationItemLambdaUpdateWrapper.set(QuickNavigationItem::getContent,quickNavigationItem.getContent());
        quickNavigationItemLambdaUpdateWrapper.set(QuickNavigationItem::getType,quickNavigationItem.getType());
        quickNavigationItemLambdaUpdateWrapper.set(QuickNavigationItem::getCategorizeId,quickNavigationItem.getCategorizeId());
        boolean update = super.update(quickNavigationItemLambdaUpdateWrapper);
        if (update){
            return R.success("更新成功");
        }
        return R.error("更新失败");
    }
}
