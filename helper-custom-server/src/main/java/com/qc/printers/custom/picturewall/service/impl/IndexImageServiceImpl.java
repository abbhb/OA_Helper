package com.qc.printers.custom.picturewall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import com.qc.printers.common.picturewall.service.IIndexImageService;
import com.qc.printers.custom.picturewall.service.IndexImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class IndexImageServiceImpl implements IndexImageService {

    @Autowired
    MinIoProperties minIoProperties;

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
        //todo:bug没正确去重
        Set<IndexImage> collect = new HashSet<>(list);
        List<String> strings = new ArrayList<>();
        for (IndexImage indexImage : collect) {
            strings.add(indexImage.getLabel());
        }
        return strings;
    }

    @Override
    public List<String> labelImage(String label) {
        LambdaQueryWrapper<IndexImage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(IndexImage::getImage).eq(IndexImage::getLabel, label);
        lambdaQueryWrapper.orderByAsc(IndexImage::getSort);
        List<IndexImage> list = iIndexImageService.list(lambdaQueryWrapper);
        List<String> strings = new ArrayList<>();
        for (IndexImage indexImage : list) {
            strings.add(minIoProperties.getUrl()+"/"+minIoProperties.getBucketName()+"/"+indexImage.getImage());
        }
        return strings;
    }
}
