package com.qc.printers.custom.picturewall.service;

import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import com.qc.printers.custom.picturewall.domain.vo.IndexImageAddReq;
import com.qc.printers.custom.picturewall.domain.vo.IndexImageAddResp;

import java.util.List;


public interface IndexImageService {
    List<String> allLabel();


    IndexImage labelImage(String label);

    String addIndexImage(IndexImageAddReq indexImageAddReq);

    String updateIndexImage(IndexImageAddReq indexImageAddReq);

    String deleteIndexImage(Long id);

    List<IndexImageAddResp> list();
}
