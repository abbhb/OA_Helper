package com.qc.printers.custom.picturewall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.picturewall.domain.entity.IndexImage;

import java.util.List;


public interface IndexImageService {
    List<String> allLabel();


    List<String> labelImage(String label);
}
