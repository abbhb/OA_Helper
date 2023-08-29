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
import java.util.List;

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
        lambdaQueryWrapper.select(IndexImage::getLabel).groupBy(IndexImage::getLabel);
        List<IndexImage> list = iIndexImageService.list(lambdaQueryWrapper);
        List<String> strings = new ArrayList<>();
        for (IndexImage indexImage : list) {
            strings.add(indexImage.getLabel());
        }
        return strings;
    }

    @Override
    public List<String> labelImage(String label) {
        LambdaQueryWrapper<IndexImage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(IndexImage::getImage).eq(IndexImage::getLabel,label);
        List<IndexImage> list = iIndexImageService.list(lambdaQueryWrapper);
        List<String> strings = new ArrayList<>();
        for (IndexImage indexImage : list) {
            strings.add(minIoProperties.getUrl()+"/"+minIoProperties.getBucketName()+"/"+indexImage.getImage());
        }
        return strings;
    }
}
