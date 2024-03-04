package com.qc.printers.custom.picturewall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import com.qc.printers.common.picturewall.service.IIndexImageService;
import com.qc.printers.custom.picturewall.service.IndexImageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class IndexImageServiceImpl implements IndexImageService {

    @Autowired
    MinIoUtil minIoUtil;

    @Autowired
    private IIndexImageService iIndexImageService;

    @Override
    public List<String> allLabel() {
        LambdaQueryWrapper<IndexImage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(IndexImage::getLabel, IndexImage::getSort);
        List<IndexImage> list = iIndexImageService.list(lambdaQueryWrapper);
        list.sort((m1, m2) -> {
            Integer order1 = m1.getSort();
            Integer order2 = m2.getSort();
            return order1.compareTo(order2);
        });
        Set<String> strings = new HashSet<>();
        List<String> stringList = new ArrayList<>();
        for (IndexImage indexImage : list) {
            if (!strings.contains(indexImage.getLabel())) {
                stringList.add(indexImage.getLabel());
            }
            strings.add(indexImage.getLabel());

        }
        return stringList;
    }

    @Override
    public List<IndexImage> labelImage(String label) {
        LambdaQueryWrapper<IndexImage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(IndexImage::getImage, IndexImage::getExtra).eq(IndexImage::getLabel, label);
        lambdaQueryWrapper.orderByAsc(IndexImage::getSort);
        List<IndexImage> list = iIndexImageService.list(lambdaQueryWrapper);
        for (IndexImage indexImage : list) {
            indexImage.setImage(OssDBUtil.toUseUrl(indexImage.getImage()));
        }
        return list;
    }


    @Transactional
    @Override
    public String addIndexImage(IndexImage indexImage) {
        if (indexImage == null) {
            throw new IllegalArgumentException("请检查");
        }
        if (StringUtils.isEmpty(indexImage.getImage())) {
            throw new IllegalArgumentException("必须包含图片");
        }
        if (StringUtils.isEmpty(indexImage.getLabel())) {
            throw new IllegalArgumentException("必须包含标签");
        }
        if (indexImage.getSort() == null) {
            throw new IllegalArgumentException("请输入排序");
        }
        indexImage.setImage(OssDBUtil.toDBUrl(indexImage.getImage()));
        iIndexImageService.save(indexImage);
        return "添加成功";
    }

    @Transactional
    @Override
    public String updateIndexImage(IndexImage indexImage) {
        if (indexImage == null) {
            throw new IllegalArgumentException("请检查");
        }
        if (indexImage.getId() == null) {
            throw new IllegalArgumentException("请检查");
        }
        if (StringUtils.isEmpty(indexImage.getImage())) {
            throw new IllegalArgumentException("必须包含图片");
        }
        if (StringUtils.isEmpty(indexImage.getLabel())) {
            throw new IllegalArgumentException("必须包含标签");
        }
        if (indexImage.getSort() == null) {
            throw new IllegalArgumentException("请输入排序");
        }
        indexImage.setImage(OssDBUtil.toDBUrl(indexImage.getImage()));
        iIndexImageService.updateById(indexImage);
        return "更新成功";
    }

    @Transactional
    @Override
    public String deleteIndexImage(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("请检查");
        }
        iIndexImageService.removeById(id);
        return "删除成功";
    }

    @Override
    public List<IndexImage> list() {
        return iIndexImageService.list().stream().peek(indexImage -> indexImage.setImage(OssDBUtil.toUseUrl(indexImage.getImage()))).toList();
    }
}
