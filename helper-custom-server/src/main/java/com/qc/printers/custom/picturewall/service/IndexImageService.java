package com.qc.printers.custom.picturewall.service;

import com.qc.printers.common.picturewall.domain.entity.IndexImage;

import java.util.List;


public interface IndexImageService {
    List<String> allLabel();


    List<IndexImage> labelImage(String label);

    String addIndexImage(IndexImage indexImage);

    String updateIndexImage(IndexImage indexImage);

    String deleteIndexImage(Long id);

    List<IndexImage> list();
}
