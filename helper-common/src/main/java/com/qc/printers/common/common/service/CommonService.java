package com.qc.printers.common.common.service;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.ToEmail;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    R<String> uploadFileTOMinio(MultipartFile file);

    String getImageUrl(String imageKey);

    R<String> sendEmailCode(ToEmail toEmail);

    Integer countApi();

    Integer apiCountLastday();

    String getAllImageUrl(String key);
}